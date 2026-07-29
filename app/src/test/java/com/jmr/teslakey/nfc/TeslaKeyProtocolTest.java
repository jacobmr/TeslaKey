/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class TeslaKeyProtocolTest {
    private FakeCredential credential;
    private TeslaKeyProtocol protocol;

    @Before
    public void setUp() {
        credential = new FakeCredential();
        protocol = new TeslaKeyProtocol(
                credential,
                destination -> {
                    destination[0] = 0x01;
                    destination[1] = 0x02;
                    destination[2] = 0x03;
                    destination[3] = 0x04;
                });
    }

    @Test
    public void selectsCardAndPhoneKeyAids() {
        assertArrayEquals(
                hex("9000"),
                protocol.process(hex("00a404000a7465736c614c6f676963")));
        assertArrayEquals(
                hex("9000"),
                protocol.process(hex("00a404000af465736c614c6f676963")));
    }

    @Test
    public void rejectsAnUnknownAid() {
        assertArrayEquals(
                hex("6a82"),
                protocol.process(hex("00a4040005f001020304")));
    }

    @Test
    public void returnsUncompressedPublicKey() {
        byte[] expected = Arrays.copyOf(credential.publicKey, 67);
        expected[65] = (byte) 0x90;
        expected[66] = 0x00;

        assertArrayEquals(expected, protocol.process(hex("8004000000")));
    }

    @Test
    public void authenticatesUsingEcdhSha1AndAes() throws Exception {
        byte[] vehicleKey = new byte[65];
        vehicleKey[0] = 0x04;
        Arrays.fill(vehicleKey, 1, vehicleKey.length, (byte) 0x33);
        byte[] challenge = hex("101112131415161718191a1b1c1d1e1f");
        byte[] data = concatenate(vehicleKey, challenge);
        byte[] command = concatenate(hex("8011000051"), data);

        byte[] saltedChallenge = challenge.clone();
        System.arraycopy(hex("01020304"), 0, saltedChallenge, 0, 4);
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(credential.sharedSecret);
        byte[] aesKey = Arrays.copyOf(digest, 16);
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        byte[] expected = concatenate(cipher.doFinal(saltedChallenge), hex("9000"));

        assertArrayEquals(expected, protocol.process(command));
        assertArrayEquals(vehicleKey, credential.lastPeerPublicKey);
    }

    @Test
    public void returnsCardFormFactor() {
        assertArrayEquals(hex("00019000"), protocol.process(hex("80140000")));
    }

    @Test
    public void rejectsWrongParametersAndMalformedBodies() {
        assertArrayEquals(hex("6a86"), protocol.process(hex("8004010000")));
        assertArrayEquals(hex("6700"), protocol.process(hex("801100005104")));
        assertArrayEquals(hex("6e00"), protocol.process(hex("00140000")));
        assertArrayEquals(hex("6d00"), protocol.process(hex("807f0000")));
    }

    private static final class FakeCredential implements TeslaCredential {
        private final byte[] publicKey = new byte[65];
        private final byte[] sharedSecret = new byte[32];
        private byte[] lastPeerPublicKey;

        private FakeCredential() {
            publicKey[0] = 0x04;
            for (int index = 1; index < publicKey.length; index++) {
                publicKey[index] = (byte) index;
            }
            for (int index = 0; index < sharedSecret.length; index++) {
                sharedSecret[index] = (byte) (0xa0 + index);
            }
        }

        @Override
        public byte[] publicKeyUncompressed() {
            return publicKey.clone();
        }

        @Override
        public byte[] agree(byte[] peerPublicKeyUncompressed) throws GeneralSecurityException {
            lastPeerPublicKey = peerPublicKeyUncompressed.clone();
            return sharedSecret.clone();
        }
    }

    private static byte[] concatenate(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static byte[] hex(String value) {
        byte[] result = new byte[value.length() / 2];
        for (int index = 0; index < result.length; index++) {
            int offset = index * 2;
            result[index] = (byte) Integer.parseInt(value.substring(offset, offset + 2), 16);
        }
        return result;
    }
}
