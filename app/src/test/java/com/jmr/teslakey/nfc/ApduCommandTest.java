/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class ApduCommandTest {
    @Test
    public void parsesCaseOneCommand() {
        ApduCommand command = ApduCommand.parse(hex("80140000"));

        assertEquals(0x80, command.cla());
        assertEquals(0x14, command.instruction());
        assertEquals(0x00, command.parameter1());
        assertEquals(0x00, command.parameter2());
        assertArrayEquals(new byte[0], command.data());
    }

    @Test
    public void parsesShortBodyWithOptionalExpectedLength() {
        ApduCommand withoutLe = ApduCommand.parse(hex("00a404000a7465736c614c6f676963"));
        ApduCommand withLe = ApduCommand.parse(hex("00a404000a7465736c614c6f67696300"));

        assertArrayEquals(hex("7465736c614c6f676963"), withoutLe.data());
        assertArrayEquals(withoutLe.data(), withLe.data());
    }

    @Test
    public void rejectsTruncatedBody() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ApduCommand.parse(hex("801100005104")));
    }

    @Test
    public void rejectsExtendedBody() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ApduCommand.parse(hex("801100000051")));
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
