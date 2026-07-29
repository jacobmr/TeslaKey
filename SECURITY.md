# Security

## Supported security model

TeslaKey is intended to act like one additional NFC key card on a personally
controlled Wear OS watch. It assumes:

- the watch operating system and lock screen are trusted;
- Android Keystore correctly reports the key's security level;
- the user keeps a separate working Tesla key card available;
- physical access to the unlocked watch is equivalent to physical access to a
  vehicle key.

The app accepts only hardware-backed credentials. It prefers StrongBox and
falls back to the watch's Trusted Execution Environment. The app displays the
reported level before declaring itself ready.

## Protections

- The P-256 private key is created with
  `KeyProperties.PURPOSE_AGREE_KEY` inside Android Keystore.
- ECDH uses the `AndroidKeyStore` provider. Only the derived shared secret
  enters app memory.
- Vehicle public keys are decoded as uncompressed P-256 points and checked for
  coordinate range and curve membership.
- APDUs are length-checked and unknown commands fail closed.
- The watch-lock check defaults to enabled and is forced on when Android Secure
  NFC is enabled.
- Backups and device transfers are disabled.
- No APDU, key, shared secret, token, VIN, or vehicle identifier is logged.
- The manifest has no internet or Bluetooth permissions.

## Important limitations

- The Tesla NFC card protocol intentionally reuses a stable ECDH credential.
  This differs from general-purpose ephemeral ECDH guidance.
- "Require unlocked watch" is enforced by checking Android's lock state before
  each APDU. The Keystore key itself is not authentication-bound because an NFC
  exchange cannot pause for a biometric prompt without timing out.
- Hardware-backed storage reduces key-extraction risk; it does not protect an
  already-unlocked, compromised watch from invoking the key.
- HCE behavior, NFC antenna placement, and background routing vary by watch and
  firmware.
- The protocol is based on public reverse-engineering, not a Tesla-supported
  third-party NFC credential API. Vehicle firmware can change behavior.
- No amount of software testing substitutes for repeated vehicle tests with a
  physical backup key available.

## Credential reset

Clearing app storage or uninstalling the app destroys its non-exportable key.
Afterward, remove the old entry from the vehicle and enroll the newly generated
credential. There is intentionally no in-app reset button.

## Reporting a vulnerability

Do not include private keys, credentials, VINs, or other secrets in an issue or
log. Share a minimal reproduction and the affected watch, Wear OS version, and
vehicle firmware version through a private channel chosen by the repository
owner.
