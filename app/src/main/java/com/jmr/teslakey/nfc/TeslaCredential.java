/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import java.security.GeneralSecurityException;

interface TeslaCredential {
    byte[] publicKeyUncompressed() throws GeneralSecurityException;

    byte[] agree(byte[] peerPublicKeyUncompressed) throws GeneralSecurityException;
}
