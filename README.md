# TeslaKey

[![Android CI](https://github.com/jacobmr/TeslaKey/actions/workflows/android.yml/badge.svg)](https://github.com/jacobmr/TeslaKey/actions/workflows/android.yml)

TeslaKey is an unofficial, local-only Wear OS NFC key-card app for compatible
Tesla vehicles. Open the app, hold the watch to the vehicle's NFC reader, and
use it like a physical key card.

It does **not** use a Tesla account, OAuth token, internet connection, Bluetooth
connection, subscription, or backend.

> **Project status:** the protocol and Android build are tested in software,
> and successful enrollment on a physical watch and vehicle was confirmed on
> July 29, 2026. Keep a physical key card available and verify lock, unlock, and
> drive authorization repeatedly before relying on the watch as a primary key.

## What works

- Enroll the watch as an additional key using an authorized physical key card.
- Lock and unlock at the driver's B-pillar NFC reader.
- Authorize driving at the center-console NFC reader.
- Keep the private P-256 credential in hardware-backed Android Keystore.
- Require the watch to be unlocked before it answers the vehicle.

TeslaKey is a manual NFC key. It does not provide passive entry, approach
unlock, walk-away locking, remote climate control, or other internet commands.

## Before you begin

You need:

- a Wear OS watch running Android 12 / API 31 or later;
- NFC hardware with host card emulation (HCE);
- hardware-backed Android Keystore support for P-256 ECDH;
- a compatible Tesla vehicle;
- one working, authorized Tesla key card;
- a TeslaKey APK; and
- either an Android phone with Wear Installer 2 or a computer with Android
  Debug Bridge (`adb`).

This repository currently publishes source code, not a signed release APK.
Developers can build a debug APK for evaluation by following
[Building from source](docs/BUILDING.md). Do not download TeslaKey APKs from
untrusted mirrors.

The upstream project reports successful use on Pixel Watch 2 with Wear OS 5.1
and Galaxy Watch 6 models with Wear OS 5. This fork's direct Keystore ECDH
implementation has completed its first successful physical enrollment; broader
watch and vehicle testing is welcome.

## Quick start

### 1. Install TeslaKey on the watch

The watch does not need a USB connection.

- **Using the paired Android phone:** install Wear Installer 2 on the phone,
  enable wireless debugging on the watch, pair the phone to the watch, and
  select the TeslaKey APK as a **Custom APK**.
- **Using a computer:** pair and connect to the watch over Wi-Fi with `adb`,
  then run `adb install -r TeslaKey.apk`.

Both methods, including every watch menu and command, are documented in
[Installing TeslaKey](docs/INSTALL.md).

### 2. Check the watch

1. Open **TeslaKey** from the watch's app list.
2. Unlock the watch if prompted.
3. Wait for **Ready — keep this screen open**.
4. Confirm that **Key storage** says **StrongBox** or trusted hardware
   (**TEE**).

Do not continue if the app reports unsupported NFC card emulation,
software-only key storage, or a key-creation error.

### 3. Add the watch to the car

1. Park the vehicle and keep an authorized physical key card with you.
2. On the vehicle touchscreen, open
   **Controls → Locks → Keys → Add Key**.
3. Keep TeslaKey open and the watch unlocked.
4. Place the watch on the center-console NFC reader shown by the vehicle.
5. After the vehicle recognizes the watch, scan the existing physical key card
   to approve the new key.
6. Rename the new entry to something clear, such as **TeslaKey Watch**.

Reader location varies by model and production date. Follow the illustration on
the vehicle screen and see [Enrolling and using the watch](docs/ENROLLMENT.md)
for model-specific guidance and a verification checklist.

### 4. Test all three operations

Keep the physical card with you during testing.

1. Lock the vehicle at the driver's B-pillar.
2. Unlock it at the driver's B-pillar.
3. Sit in the driver's seat and authorize driving at the console reader.

Open TeslaKey and keep the watch unlocked for each test. Exact antenna placement
varies by watch, so move it slowly across the reader until the vehicle responds.

## Everyday use

- **Lock or unlock:** open TeslaKey, keep its screen awake, and hold the watch
  against the driver's B-pillar reader.
- **Authorize driving:** open TeslaKey and hold the watch against the
  center-console reader. Then press the brake within the vehicle's
  authorization window.

The app deliberately has no passive unlock mode. Version 1 is manual NFC only.

## Important update and recovery rule

Android only installs an app update when the new APK is signed by the same key
as the installed APK. Updating a locally built debug APK from a different
computer commonly fails because the signing key differs.

Uninstalling TeslaKey or clearing its app data destroys the non-exportable
Android Keystore credential. If that happens:

1. remove the old TeslaKey entry from **Controls → Locks → Keys**;
2. install the new APK;
3. open it to create a new hardware-backed credential; and
4. enroll the watch again.

Never uninstall as a first troubleshooting step. See
[Troubleshooting](docs/TROUBLESHOOTING.md) first.

## Security properties

- The stable P-256 credential is generated inside Android Keystore.
- ECDH operations happen inside Android Keystore; the private key is not
  exported into app memory.
- StrongBox is preferred when the watch supports it. Trusted Execution
  Environment (TEE) storage is the fallback.
- The app refuses to become ready if Android reports software-only key storage.
- **Require unlocked watch** is enabled by default.
- App backup and device-to-device data transfer are disabled.
- The app requests NFC permission only. It does not request internet, Bluetooth,
  location, account, or storage access.

Read [SECURITY.md](SECURITY.md) and
[the cryptography notes](docs/CRYPTOGRAPHY.md) for the threat model and protocol
audit.

## Documentation

- [Install from an Android phone or computer](docs/INSTALL.md)
- [Enroll, test, and use the watch](docs/ENROLLMENT.md)
- [Troubleshooting and recovery](docs/TROUBLESHOOTING.md)
- [Build and sign from source](docs/BUILDING.md)
- [Security model](SECURITY.md)
- [Cryptography and protocol audit](docs/CRYPTOGRAPHY.md)
- [Roadmap](docs/ROADMAP.md)
- [Contributing](CONTRIBUTING.md)

## Origin and license

This project is a GPL-3.0-only derivative of
[pccr10001/TeslaWearKey](https://github.com/pccr10001/TeslaWearKey), audited and
reworked under package ID `com.jmr.teslakey`. Protocol behavior is based on
Robert Quattlebaum's
[Tesla Key Card Protocol research](https://gist.github.com/darconeous/2cd2de11148e3a75685940158bddf933).

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and [LICENSE](LICENSE).
TeslaKey is not affiliated with or endorsed by Tesla, Inc. Tesla and related
marks belong to their respective owner.

## Contributing

Bug reports, device compatibility results, protocol review, and focused pull
requests are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting
changes. Never include VINs, private keys, credentials, pairing codes, or
identifying vehicle logs in a public issue.
