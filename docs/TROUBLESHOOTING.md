# Troubleshooting

Start with the section that matches the failure. Keep a physical key card
available and do not uninstall TeslaKey unless the recovery steps specifically
require it.

## The phone or computer cannot see the watch

1. Confirm the watch and installer device are on the same Wi-Fi network.
2. On the watch, confirm both **ADB debugging** and **Wireless debugging** are
   enabled.
3. Keep the Wireless debugging screen open while pairing.
4. Turn Wireless debugging off and back on.
5. Restart the phone or computer and the watch.
6. Try a personal hotspot if the current network isolates connected devices.

Bluetooth pairing between the phone and watch is not the ADB connection. The
installer still needs the watch's wireless-debugging IP address and port.

For computer installation, restart ADB:

```bash
adb kill-server
adb start-server
adb devices
```

## Pairing succeeds but connection fails

The pairing and connection ports are normally different.

1. Use **Pair new device** only for the six-digit code and pairing port.
2. After pairing, return to the main **Wireless debugging** screen.
3. Use the IP address and new port shown there for Wear Installer 2 or
   `adb connect`.

Wireless-debugging ports can change after the feature, watch, or network
restarts. Read the current port from the watch each time.

## ADB reports more than one device

Target the watch by its full wireless-debugging address:

```bash
adb -s 192.168.1.50:38841 install -r /path/to/TeslaKey.apk
```

Use the address shown by:

```bash
adb devices
```

Do not accidentally install the Wear OS APK on the connected phone.

## The APK will not install or update

Common ADB errors:

### `INSTALL_FAILED_OLDER_SDK`

The watch's Android API level is below TeslaKey's minimum API 31 requirement.

### `INSTALL_FAILED_MISSING_FEATURE`

The watch does not advertise a required Wear OS, NFC, or HCE feature.

### `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

The installed app and new APK were signed by different certificates. This is
common when switching between debug builds made on different computers or
between a debug build and a future official release.

Do not uninstall immediately. First:

1. make sure an authorized physical key card works;
2. remove the existing TeslaKey entry from the vehicle;
3. uninstall the old app;
4. install the new APK; and
5. enroll the new credential.

Uninstalling destroys the old app's Android Keystore credential.

### `adb: more than one device/emulator`

Use the targeted `adb -s WATCH_IP:PORT ...` form shown above.

## TeslaKey says NFC card emulation is unsupported

The watch does not expose the HCE feature required to emulate a key card.
Having NFC payments does not guarantee that third-party HCE is available.

Check for a watch OS update. If the error remains, the current watch/firmware
combination is not supported.

## TeslaKey says the key is not hardware protected

TeslaKey intentionally refuses software-backed credentials.

1. Restart the watch.
2. Install current watch firmware.
3. Open TeslaKey again.

If the watch still reports software-only storage, do not bypass the check. Use a
different supported watch.

## TeslaKey cannot create the local key

1. Confirm the watch has a PIN, pattern, or other secure lock configured.
2. Restart the watch.
3. Check for a watch OS update.
4. Reopen TeslaKey.

Do not clear app storage or uninstall if the watch has already been enrolled;
that destroys the existing credential.

## The car does not react to the watch

Check each item:

- The watch is awake and unlocked.
- TeslaKey is open in the foreground.
- The screen reads **Ready — keep this screen open**.
- Key storage reports **StrongBox** or trusted hardware (**TEE**).
- You are using the correct vehicle reader for the operation.
- The watch is held still long enough for an NFC exchange.

Test the physical Tesla key card at the same reader. If the physical card also
fails, correct the reader location or vehicle state first.

Move the watch slowly through several orientations. NFC antennas are not always
centered behind the watch face.

## The car recognizes the watch but does not finish enrollment

Enrollment requires two separate scans:

1. scan the new TeslaKey watch credential; then
2. scan an already-authorized physical key card to approve it.

Wait for the vehicle prompt or chime between scans. If the attempt times out,
cancel **Add Key** and begin again.

## B-pillar access works but the car will not drive

Driving uses the console NFC reader, not the B-pillar.

1. Sit in the driver's seat.
2. Open TeslaKey and unlock the watch.
3. Hold the watch against the console reader.
4. Press the brake promptly after the vehicle accepts the key.

If the authorization window expires, scan again.

## Driving works but the B-pillar does not

Use the driver's B-pillar reader, normally below the side camera on Model 3/Y.
Do not use the center-console location for exterior locking and unlocking.

## It worked before a watch or vehicle update

1. Restart the watch.
2. Verify TeslaKey still says **Ready**.
3. Retest exact antenna placement.
4. Test the physical card at the same reader.
5. Report the watch model, Wear OS version, vehicle model/year, and vehicle
   software version if the regression remains.

Do not post a VIN, pairing code, private key, account information, or identifying
vehicle logs.

## Safely report a problem

Use the repository's issue templates and include:

- watch manufacturer and model;
- Wear OS and Android versions;
- Tesla model and model year;
- Tesla software version;
- TeslaKey version or commit;
- whether installation, enrollment, B-pillar access, or driving failed;
- the exact displayed error, without personal information; and
- whether the physical key card works at the same reader.

For a security vulnerability, follow [SECURITY.md](../SECURITY.md) and do not
open a public issue.
