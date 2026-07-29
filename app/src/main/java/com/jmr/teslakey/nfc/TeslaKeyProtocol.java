/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import android.annotation.SuppressLint;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

final class TeslaKeyProtocol {
    interface RandomSource {
        void nextBytes(byte[] destination);
    }

    private static final int INS_SELECT = 0xa4;
    private static final int INS_GET_PUBLIC_KEY = 0x04;
    private static final int INS_AUTHENTICATE = 0x11;
    private static final int INS_GET_FORM_FACTOR = 0x14;

    private static final byte[] CARD_AID = hex("7465736c614c6f676963");
    private static final byte[] PHONE_KEY_AID = hex("f465736c614c6f676963");

    private static final byte[] SW_SUCCESS = status(0x90, 0x00);
    private static final byte[] SW_FILE_NOT_FOUND = status(0x6a, 0x82);
    private static final byte[] SW_CONDITIONS_NOT_SATISFIED = status(0x69, 0x85);
    private static final byte[] SW_WRONG_DATA = status(0x6a, 0x80);
    private static final byte[] SW_WRONG_PARAMETERS = status(0x6a, 0x86);
    private static final byte[] SW_WRONG_LENGTH = status(0x67, 0x00);
    private static final byte[] SW_CLASS_NOT_SUPPORTED = status(0x6e, 0x00);
    private static final byte[] SW_INSTRUCTION_NOT_SUPPORTED = status(0x6d, 0x00);
    private static final byte[] SW_UNKNOWN = status(0x6f, 0x00);

    private final TeslaCredential credential;
    private final RandomSource random;

    TeslaKeyProtocol(TeslaCredential credential) {
        SecureRandom secureRandom = new SecureRandom();
        this.credential = credential;
        this.random = secureRandom::nextBytes;
    }

    TeslaKeyProtocol(TeslaCredential credential, RandomSource random) {
        this.credential = credential;
        this.random = random;
    }

    byte[] process(byte[] encodedCommand) {
        final ApduCommand command;
        try {
            command = ApduCommand.parse(encodedCommand);
        } catch (IllegalArgumentException malformed) {
            return SW_WRONG_LENGTH.clone();
        }

        if (command.cla() == 0x00 && command.instruction() == INS_SELECT) {
            return processSelect(command);
        }
        if (command.cla() != 0x80) {
            return SW_CLASS_NOT_SUPPORTED.clone();
        }
        if (command.parameter1() != 0x00 || command.parameter2() != 0x00) {
            return SW_WRONG_PARAMETERS.clone();
        }

        try {
            return switch (command.instruction()) {
                case INS_GET_PUBLIC_KEY -> processGetPublicKey(command);
                case INS_AUTHENTICATE -> processAuthenticate(command);
                case INS_GET_FORM_FACTOR -> processGetFormFactor(command);
                default -> SW_INSTRUCTION_NOT_SUPPORTED.clone();
            };
        } catch (GeneralSecurityException | RuntimeException failure) {
            return SW_UNKNOWN.clone();
        }
    }

    static byte[] conditionsNotSatisfied() {
        return SW_CONDITIONS_NOT_SATISFIED.clone();
    }

    private byte[] processSelect(ApduCommand command) {
        if (command.parameter1() != 0x04 || command.parameter2() != 0x00) {
            return SW_WRONG_PARAMETERS.clone();
        }

        byte[] aid = command.data();
        if (Arrays.equals(aid, CARD_AID) || Arrays.equals(aid, PHONE_KEY_AID)) {
            return SW_SUCCESS.clone();
        }
        return SW_FILE_NOT_FOUND.clone();
    }

    private byte[] processGetPublicKey(ApduCommand command) throws GeneralSecurityException {
        if (command.data().length != 0) {
            return SW_WRONG_LENGTH.clone();
        }

        byte[] publicKey = credential.publicKeyUncompressed();
        if (publicKey.length != 65 || publicKey[0] != 0x04) {
            return SW_WRONG_DATA.clone();
        }
        return appendStatus(publicKey, SW_SUCCESS);
    }

    @SuppressLint("GetInstance") // The Tesla protocol encrypts exactly one AES block.
    private byte[] processAuthenticate(ApduCommand command) throws GeneralSecurityException {
        byte[] request = command.data();
        if (request.length != 81) {
            return SW_WRONG_LENGTH.clone();
        }

        byte[] peerPublicKey = Arrays.copyOfRange(request, 0, 65);
        byte[] challenge = Arrays.copyOfRange(request, 65, 81);
        byte[] salt = new byte[4];
        byte[] sharedSecret = null;
        byte[] digest = null;
        byte[] aesKey = null;
        try {
            random.nextBytes(salt);
            System.arraycopy(salt, 0, challenge, 0, salt.length);

            sharedSecret = credential.agree(peerPublicKey);
            digest = MessageDigest.getInstance("SHA-1").digest(sharedSecret);
            aesKey = Arrays.copyOf(digest, 16);

            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
            return appendStatus(cipher.doFinal(challenge), SW_SUCCESS);
        } finally {
            Arrays.fill(peerPublicKey, (byte) 0);
            Arrays.fill(challenge, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            clear(sharedSecret);
            clear(digest);
            clear(aesKey);
            Arrays.fill(request, (byte) 0);
        }
    }

    private byte[] processGetFormFactor(ApduCommand command) {
        if (command.data().length != 0) {
            return SW_WRONG_LENGTH.clone();
        }
        return new byte[]{0x00, 0x01, (byte) 0x90, 0x00};
    }

    private static byte[] appendStatus(byte[] data, byte[] status) {
        byte[] response = Arrays.copyOf(data, data.length + status.length);
        System.arraycopy(status, 0, response, data.length, status.length);
        return response;
    }

    private static byte[] status(int first, int second) {
        return new byte[]{(byte) first, (byte) second};
    }

    private static byte[] hex(String value) {
        byte[] decoded = new byte[value.length() / 2];
        for (int index = 0; index < decoded.length; index++) {
            int offset = index * 2;
            decoded[index] = (byte) Integer.parseInt(value.substring(offset, offset + 2), 16);
        }
        return decoded;
    }

    private static void clear(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }
}
