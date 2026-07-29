/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

public final class AndroidKeystoreCredential implements TeslaCredential {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "tesla_key_card_v1";
    private static final String CURVE = "secp256r1";
    private static final Object KEY_CREATION_LOCK = new Object();

    private final Context context;
    private final KeyStore keyStore;

    public AndroidKeystoreCredential(Context context) throws GeneralSecurityException {
        this.context = context.getApplicationContext();
        keyStore = KeyStore.getInstance(KEYSTORE);
        try {
            keyStore.load(null);
        } catch (IOException failure) {
            throw new GeneralSecurityException("Could not load Android Keystore", failure);
        }
    }

    public void ensureKeyExists() throws GeneralSecurityException {
        synchronized (KEY_CREATION_LOCK) {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                return;
            }

            boolean strongBoxAvailable = context.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE);
            if (strongBoxAvailable) {
                try {
                    generateKey(true);
                    return;
                } catch (ProviderException unavailable) {
                    if (keyStore.containsAlias(KEY_ALIAS)) {
                        return;
                    }
                }
            }
            generateKey(false);
        }
    }

    public boolean keyExists() {
        try {
            return keyStore.containsAlias(KEY_ALIAS);
        } catch (GeneralSecurityException failure) {
            return false;
        }
    }

    public String securityLevelDescription() throws GeneralSecurityException {
        KeyInfo keyInfo = keyInfo();
        return switch (keyInfo.getSecurityLevel()) {
            case KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox hardware";
            case KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "trusted hardware (TEE)";
            case KeyProperties.SECURITY_LEVEL_SOFTWARE -> "software-backed";
            case KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE -> "secure hardware";
            case KeyProperties.SECURITY_LEVEL_UNKNOWN -> "unknown";
            default -> "unrecognized";
        };
    }

    public boolean isHardwareBacked() throws GeneralSecurityException {
        int level = keyInfo().getSecurityLevel();
        return level == KeyProperties.SECURITY_LEVEL_STRONGBOX
                || level == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT
                || level == KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE;
    }

    @Override
    public byte[] publicKeyUncompressed() throws GeneralSecurityException {
        ECPublicKey publicKey = publicKey();
        byte[] encoded = new byte[65];
        encoded[0] = 0x04;
        copyUnsignedFixed(publicKey.getW().getAffineX(), encoded, 1, 32);
        copyUnsignedFixed(publicKey.getW().getAffineY(), encoded, 33, 32);
        return encoded;
    }

    @Override
    public byte[] agree(byte[] peerPublicKeyUncompressed) throws GeneralSecurityException {
        ECPublicKey localPublicKey = publicKey();
        PublicKey peerPublicKey = decodeAndValidatePeer(
                peerPublicKeyUncompressed,
                localPublicKey.getParams());

        KeyAgreement agreement = KeyAgreement.getInstance("ECDH", KEYSTORE);
        agreement.init(privateKey());
        agreement.doPhase(peerPublicKey, true);
        return agreement.generateSecret();
    }

    private void generateKey(boolean useStrongBox) throws GeneralSecurityException {
        KeyGenParameterSpec.Builder parameters = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_AGREE_KEY)
                .setAlgorithmParameterSpec(new ECGenParameterSpec(CURVE))
                .setUserAuthenticationRequired(false);
        if (useStrongBox) {
            parameters.setIsStrongBoxBacked(true);
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                KEYSTORE);
        generator.initialize(parameters.build());
        generator.generateKeyPair();
    }

    private KeyInfo keyInfo() throws GeneralSecurityException {
        PrivateKey privateKey = privateKey();
        KeyFactory keyFactory = KeyFactory.getInstance(privateKey.getAlgorithm(), KEYSTORE);
        return keyFactory.getKeySpec(privateKey, KeyInfo.class);
    }

    private PrivateKey privateKey() throws GeneralSecurityException {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
        if (privateKey == null) {
            throw new GeneralSecurityException("Tesla credential is missing");
        }
        return privateKey;
    }

    private ECPublicKey publicKey() throws GeneralSecurityException {
        java.security.cert.Certificate certificate = keyStore.getCertificate(KEY_ALIAS);
        if (certificate == null || !(certificate.getPublicKey() instanceof ECPublicKey)) {
            throw new GeneralSecurityException("Tesla credential public key is missing");
        }
        return (ECPublicKey) certificate.getPublicKey();
    }

    private static PublicKey decodeAndValidatePeer(byte[] encoded, ECParameterSpec parameters)
            throws GeneralSecurityException {
        if (encoded == null || encoded.length != 65 || encoded[0] != 0x04) {
            throw new GeneralSecurityException("Peer key must be an uncompressed P-256 point");
        }

        BigInteger x = new BigInteger(1, Arrays.copyOfRange(encoded, 1, 33));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(encoded, 33, 65));
        validatePoint(x, y, parameters);

        ECPublicKeySpec keySpec = new ECPublicKeySpec(new ECPoint(x, y), parameters);
        return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC).generatePublic(keySpec);
    }

    private static void validatePoint(
            BigInteger x,
            BigInteger y,
            ECParameterSpec parameters) throws GeneralSecurityException {
        ECField field = parameters.getCurve().getField();
        if (!(field instanceof ECFieldFp) || field.getFieldSize() != 256) {
            throw new GeneralSecurityException("Credential curve is not P-256");
        }

        BigInteger prime = ((ECFieldFp) field).getP();
        if (x.signum() < 0 || y.signum() < 0
                || x.compareTo(prime) >= 0 || y.compareTo(prime) >= 0) {
            throw new GeneralSecurityException("Peer key coordinates are out of range");
        }

        BigInteger left = y.modPow(BigInteger.valueOf(2), prime);
        BigInteger right = x.modPow(BigInteger.valueOf(3), prime)
                .add(parameters.getCurve().getA().multiply(x))
                .add(parameters.getCurve().getB())
                .mod(prime);
        if (!left.equals(right)) {
            throw new GeneralSecurityException("Peer key is not on P-256");
        }
    }

    private static void copyUnsignedFixed(
            BigInteger value,
            byte[] destination,
            int destinationOffset,
            int size) throws GeneralSecurityException {
        byte[] source = value.toByteArray();
        if (source.length > size + 1 || (source.length == size + 1 && source[0] != 0)) {
            throw new GeneralSecurityException("EC coordinate is too large");
        }
        int sourceOffset = source.length == size + 1 ? 1 : 0;
        int length = source.length - sourceOffset;
        System.arraycopy(source, sourceOffset, destination, destinationOffset + size - length, length);
        Arrays.fill(source, (byte) 0);
    }
}
