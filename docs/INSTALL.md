# Installing TeslaKey

TeslaKey is a standalone Wear OS app. It must be installed on the **watch**, not
on the companion phone. The watch does not need to connect to a computer by
cable.

Choose one installation method:

- [Method A: Android phone and Wear Installer 2](#method-a-android-phone-and-wear-installer-2)
- [Method B: computer and ADB over Wi-Fi](#method-b-computer-and-adb-over-wi-fi)

Method A is usually easiest for someone who already has the APK on an Android
phone. Method B uses Google's standard command-line tools and is easier to
diagnose.

## Before installing

### Check the watch

TeslaKey requires all of the following:

- Wear OS based on Android 12 / API 31 or later;
- an NFC radio;
- NFC host card emulation (HCE); and
- hardware-backed P-256 ECDH in Android Keystore.

The app checks the last two requirements when it starts. A watch can have NFC
for payments but still differ in its third-party HCE or Keystore support.

### Obtain an APK

This repository currently publishes source code and does not yet publish a
maintainer-signed release APK.

For development or evaluation, build:

```text
app/build/outputs/apk/debug/app-debug.apk
```

using [Building from source](BUILDING.md). Copy that APK to the Android phone if
you plan to use Method A.

Do not install an APK from an unofficial mirror. An APK can display the same
name and icon while containing different code.

### Understand the signing-key rule

Every Android APK is signed. Android accepts an update only when it is signed by
the same certificate as the installed version.

- Rebuilding on the same computer normally reuses that computer's debug key.
- Building on another computer normally produces a different debug key.
- A future official release will use a dedicated release key, not a developer's
  debug key.

If signatures differ, Android reports an incompatible update. Installing the
new APK then requires removing the old app, which also destroys the enrolled
TeslaKey credential. Remove the old key from the vehicle and plan to enroll
again before uninstalling.

## Enable developer options on the watch

Menu names vary slightly by watch manufacturer and Wear OS version.

1. On the watch, open **Settings**.
2. Open **System → About → Versions**. On some watches it is simply
   **System → About**.
3. Find **Build number** and tap it seven times.
4. Enter the watch PIN or pattern if requested.
5. Return to **Settings** and open **Developer options**.
6. Enable **ADB debugging**.
7. Enable **Wireless debugging**.
8. Accept the confirmation and choose **Always allow on this network** if the
   watch offers that option.

The watch and the phone or computer must be on the same Wi-Fi network. Networks
with client or access-point isolation can prevent the connection even when both
devices show the same network name. A personal hotspot can be a useful
alternative.

Google's current reference is
[Debug Wear OS over Wi-Fi](https://developer.android.com/training/wearables/get-started/debug-wifi).

## Method A: Android phone and Wear Installer 2

[Wear Installer 2](https://play.google.com/store/apps/details?id=org.freepoc.wearinstaller2)
is a third-party Android utility that uses ADB over Wi-Fi. TeslaKey is not
affiliated with it, and it does not need to be installed on the watch.

### Pair the phone to the watch

1. Install and open Wear Installer 2 on the Android phone.
2. On the watch, open
   **Settings → Developer options → Wireless debugging**.
3. On the main Wireless debugging screen, note the watch's IP address.
4. Enter that IP address in Wear Installer 2's connection field before starting
   pairing.
5. In Wear Installer 2, open its menu and select **Pair with watch**, then
   select **Enable**.
6. On the watch, select **Pair new device**.
7. The watch now displays:

   - a six-digit pairing code; and
   - a pairing port, normally five digits.

8. In Wear Installer 2's pairing dialog, enter the pairing code, a space, and
   the pairing port. For example:

   ```text
   123456 37123
   ```

9. Select **Done** and wait for **Pairing successful**.

### Connect to the watch

1. Return to the watch's main **Wireless debugging** screen.
2. Read the IP address and port displayed there.
3. Enter that complete address in Wear Installer 2. For example:

   ```text
   192.168.1.50:38841
   ```

The **connection port is usually different from the pairing port**. Pairing can
succeed while installation fails if the pairing port is reused as the
connection port.

### Install the APK

1. In Wear Installer 2, choose **Custom APK**.
2. Select the TeslaKey APK from the phone, commonly from **Downloads**.
3. Select **Install**.
4. Approve any ADB authorization prompt that appears on the watch.
5. Wait for the installer to report success.
6. Press the watch's app-list button and open **TeslaKey**.
7. Wait for **Ready — keep this screen open**.
8. Confirm the reported key storage is **StrongBox** or trusted hardware
   (**TEE**).

The developer's current Wear Installer 2 instructions are available on its
[help page](https://freepoc.org/wear-installer-2-help-page/).

## Method B: computer and ADB over Wi-Fi

### Install ADB

Install Android SDK Platform Tools from
[Android Developers](https://developer.android.com/tools/releases/platform-tools)
or use the copy bundled with Android Studio.

Verify that ADB is recent enough:

```bash
adb version
```

Google requires ADB 30.0.0 or later for this wireless-debugging flow.

### Pair the computer to the watch

1. Put the computer and watch on the same Wi-Fi network.
2. On the watch, open
   **Settings → Developer options → Wireless debugging → Pair new device**.
3. Note the IP address, pairing port, and six-digit pairing code.
4. On the computer, run:

   ```bash
   adb pair 192.168.1.50:37123
   ```

5. Enter the six-digit code when prompted.
6. Confirm that ADB reports **Successfully paired**.

Replace the example address and port with the values on the watch.

### Connect to the watch

1. Return to the watch's main **Wireless debugging** screen.
2. Note the connection IP address and port. This port normally differs from the
   pairing port.
3. Run:

   ```bash
   adb connect 192.168.1.50:38841
   adb devices
   ```

4. Confirm that the watch address appears with status `device`.

If both a phone and watch are visible to ADB, always target the watch explicitly:

```bash
adb -s 192.168.1.50:38841 install -r /path/to/TeslaKey.apk
```

If the watch is the only connected ADB device, the shorter form also works:

```bash
adb install -r /path/to/TeslaKey.apk
```

ADB should finish with `Success`.

### Confirm the installation

Optionally verify the package from the computer:

```bash
adb -s 192.168.1.50:38841 shell pm list packages com.jmr.teslakey
```

Expected output:

```text
package:com.jmr.teslakey
```

Then open **TeslaKey** from the watch app list and confirm:

- **Ready — keep this screen open**; and
- **Key storage: StrongBox** or trusted hardware (**TEE**).

## After installation

Turn off **Wireless debugging** and **ADB debugging** on the watch. Neither is
needed for TeslaKey operation or vehicle enrollment.

Continue with [Enrolling and using the watch](ENROLLMENT.md).

## Updating TeslaKey

When the update is signed with the same certificate:

```bash
adb -s 192.168.1.50:38841 install -r /path/to/new-TeslaKey.apk
```

The `-r` option replaces the app while preserving its data and Keystore
credential.

Before any update:

1. keep a physical key card available;
2. confirm the new APK came from a trusted source;
3. do not uninstall the working app; and
4. retest lock, unlock, and drive authorization after updating.

If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, stop and read
[Troubleshooting](TROUBLESHOOTING.md#the-apk-will-not-install-or-update).
