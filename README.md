# TeslaKey

TeslaKey is an unofficial, local-only Wear OS NFC key-card app for compatible
Tesla vehicles. It does not use a Tesla account, OAuth token, internet
connection, Bluetooth connection, subscription, or backend.

The app currently implements only Version 1: open the app and hold the watch to
the vehicle's NFC reader. It can be enrolled as a new key using an existing
authorized key card.

> **Project status:** the protocol and Android build are tested in software,
> and successful enrollment on a physical watch and vehicle was confirmed on
> July 29, 2026. Keep a physical key card available and verify lock, unlock, and
> drive authorization repeatedly before relying on the watch as a primary key.

## Security properties

- The stable P-256 credential is generated inside Android Keystore.
- ECDH operations happen inside Android Keystore; the private key is not
  exported into app memory.
- StrongBox is preferred when the watch supports it. Trusted Execution
  Environment (TEE) storage is the fallback.
- The app refuses to become ready if Android reports software-only key storage.
- "Require unlocked watch" is enabled by default.
- App backup and device-to-device data transfer are disabled.
- The app requests NFC permission only. It does not request internet, Bluetooth,
  location, account, or storage access.

See [SECURITY.md](SECURITY.md) and
[docs/CRYPTOGRAPHY.md](docs/CRYPTOGRAPHY.md) for the threat model and protocol
audit.

## Requirements

- Wear OS watch running Android 12 / API 31 or later
- NFC hardware with host card emulation (HCE)
- Hardware-backed Android Keystore support for P-256 ECDH
- A compatible Tesla vehicle and one existing authorized key card
- Android platform tools (`adb`) for sideloading

The upstream project reports successful use on Pixel Watch 2 with Wear OS 5.1
and Galaxy Watch 6 models with Wear OS 5. This fork's direct Keystore ECDH
implementation has completed its first successful physical enrollment; broader
watch and vehicle testing is welcome.

## Build

This checkout uses the Gradle wrapper, Android Gradle Plugin 8.9.3, Java 17
source compatibility, and Android SDK 35.

On this Mac, Android Studio's bundled JDK can run the build:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew testDebugUnitTest assembleDebug
```

The resulting debug APK is:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Android's debug build is signed automatically and can be sideloaded for device
testing:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

For long-term use, create a dedicated release signing key outside this
repository and keep it backed up. Losing or changing the app signature requires
uninstalling the old app, which deletes the enrolled watch credential. No
release keystore or password belongs in Git.

## Enroll

1. Install TeslaKey on the watch.
2. Open it once. Confirm the screen says **Ready** and reports StrongBox or
   trusted hardware (TEE).
3. In the parked vehicle, open **Controls → Locks** and start adding a key.
4. With TeslaKey open, place the watch against the console NFC reader.
5. Confirm the new credential using an existing authorized Tesla key card.
6. Give the new key a recognizable name in the vehicle.

Exact reader placement varies by watch. Move the watch slowly and try the side
or face nearest its NFC antenna.

## Use

- **Lock or unlock:** open TeslaKey, keep the screen awake, and hold the watch
  against the driver's B-pillar reader.
- **Authorize driving:** open TeslaKey and hold the watch against the console
  reader.

The app deliberately has no passive unlock mode. Version 1 is manual NFC only.

## Recovery and removal

Clearing app data or uninstalling TeslaKey destroys the Android Keystore
credential. It cannot be recovered from a backup. Remove the corresponding stale
key from the vehicle's Locks screen, then reinstall and enroll a new credential.

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
changes. Never include VINs, private keys, credentials, or identifying vehicle
logs in a public issue.
