/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import java.util.Arrays;

final class ApduCommand {
    private final int cla;
    private final int instruction;
    private final int parameter1;
    private final int parameter2;
    private final byte[] data;

    private ApduCommand(
            int cla,
            int instruction,
            int parameter1,
            int parameter2,
            byte[] data) {
        this.cla = cla;
        this.instruction = instruction;
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        this.data = data;
    }

    static ApduCommand parse(byte[] encoded) {
        if (encoded == null || encoded.length < 4) {
            throw new IllegalArgumentException("APDU header is incomplete");
        }

        byte[] data = new byte[0];
        if (encoded.length > 5) {
            int lengthByte = unsigned(encoded[4]);
            if (lengthByte == 0) {
                throw new IllegalArgumentException("Extended APDUs are not supported");
            }

            int withoutExpectedLength = 5 + lengthByte;
            int withExpectedLength = withoutExpectedLength + 1;
            if (encoded.length != withoutExpectedLength && encoded.length != withExpectedLength) {
                throw new IllegalArgumentException("APDU body length does not match Lc");
            }
            data = Arrays.copyOfRange(encoded, 5, withoutExpectedLength);
        }

        return new ApduCommand(
                unsigned(encoded[0]),
                unsigned(encoded[1]),
                unsigned(encoded[2]),
                unsigned(encoded[3]),
                data);
    }

    int cla() {
        return cla;
    }

    int instruction() {
        return instruction;
    }

    int parameter1() {
        return parameter1;
    }

    int parameter2() {
        return parameter2;
    }

    byte[] data() {
        return data.clone();
    }

    private static int unsigned(byte value) {
        return value & 0xff;
    }
}
