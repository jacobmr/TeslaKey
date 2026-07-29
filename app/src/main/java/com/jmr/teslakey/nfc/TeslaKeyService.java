/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey.nfc;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import java.security.GeneralSecurityException;

public final class TeslaKeyService extends HostApduService {
    public static final String PREFERENCES = "tesla_key_settings";
    public static final String REQUIRE_UNLOCK = "require_unlocked_watch";

    private TeslaKeyProtocol protocol;
    private AndroidKeystoreCredential credential;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        try {
            credential = new AndroidKeystoreCredential(this);
            if (credential.keyExists() && !credential.isHardwareBacked()) {
                credential = null;
                protocol = null;
                return;
            }
            protocol = new TeslaKeyProtocol(credential);
        } catch (GeneralSecurityException unavailable) {
            credential = null;
            protocol = null;
        }
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        boolean requireUnlock = preferences.getBoolean(REQUIRE_UNLOCK, true);
        if (requireUnlock && isWatchLocked()) {
            return TeslaKeyProtocol.conditionsNotSatisfied();
        }
        if (credential == null || protocol == null || !credential.keyExists()) {
            return TeslaKeyProtocol.conditionsNotSatisfied();
        }
        return protocol.process(commandApdu);
    }

    @Override
    public void onDeactivated(int reason) {
        // The protocol is stateless between APDUs.
    }

    private boolean isWatchLocked() {
        KeyguardManager keyguard = getSystemService(KeyguardManager.class);
        return keyguard != null && keyguard.isDeviceLocked();
    }
}
