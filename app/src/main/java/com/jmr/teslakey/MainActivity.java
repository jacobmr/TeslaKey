/*
 * Copyright 2026 JMR
 *
 * This file is part of TeslaKey and is licensed under GPL-3.0-only.
 */
package com.jmr.teslakey;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;

import com.jmr.teslakey.nfc.AndroidKeystoreCredential;
import com.jmr.teslakey.nfc.TeslaKeyService;

import java.security.GeneralSecurityException;

public final class MainActivity extends Activity {
    private NfcAdapter nfcAdapter;
    private ComponentName serviceComponent;
    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView status = findViewById(R.id.status);
        TextView keySecurity = findViewById(R.id.key_security);
        Switch requireUnlock = findViewById(R.id.require_unlock);

        SharedPreferences preferences = getSharedPreferences(
                TeslaKeyService.PREFERENCES,
                Context.MODE_PRIVATE);
        requireUnlock.setChecked(preferences.getBoolean(TeslaKeyService.REQUIRE_UNLOCK, true));
        requireUnlock.setOnCheckedChangeListener((button, checked) ->
                preferences.edit()
                        .putBoolean(TeslaKeyService.REQUIRE_UNLOCK, checked)
                        .apply());

        PackageManager packageManager = getPackageManager();
        boolean hceSupported = packageManager.hasSystemFeature(
                PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!hceSupported || nfcAdapter == null) {
            status.setText(R.string.status_unsupported);
            requireUnlock.setEnabled(false);
            return;
        }

        if (isSecureNfcEnabled(nfcAdapter)) {
            requireUnlock.setChecked(true);
            requireUnlock.setEnabled(false);
            requireUnlock.setText(R.string.secure_nfc_forces_unlock);
        }

        try {
            AndroidKeystoreCredential credential = new AndroidKeystoreCredential(this);
            credential.ensureKeyExists();
            keySecurity.setText(getString(
                    R.string.key_security_format,
                    credential.securityLevelDescription()));
            if (!credential.isHardwareBacked()) {
                status.setText(R.string.status_key_not_hardware);
                requireUnlock.setEnabled(false);
                return;
            }
            status.setText(R.string.status_ready);
            serviceComponent = new ComponentName(this, TeslaKeyService.class);
            ready = true;
        } catch (GeneralSecurityException | RuntimeException failure) {
            status.setText(R.string.status_key_error);
            requireUnlock.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ready || nfcAdapter == null || serviceComponent == null) {
            return;
        }
        try {
            CardEmulation.getInstance(nfcAdapter).setPreferredService(this, serviceComponent);
        } catch (RuntimeException ignored) {
            // AID routing still works on devices that do not expose foreground preference.
        }
    }

    @Override
    protected void onPause() {
        if (ready && nfcAdapter != null) {
            try {
                CardEmulation.getInstance(nfcAdapter).unsetPreferredService(this);
            } catch (RuntimeException ignored) {
                // Nothing to clean up when foreground preference is unsupported.
            }
        }
        super.onPause();
    }

    private static boolean isSecureNfcEnabled(NfcAdapter adapter) {
        try {
            return adapter.isSecureNfcEnabled();
        } catch (RuntimeException unavailable) {
            return false;
        }
    }
}
