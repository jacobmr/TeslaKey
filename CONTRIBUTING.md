# Contributing

Thanks for helping improve TeslaKey.

## Before opening an issue

- Confirm the watch runs Android 12 / API 31 or later.
- Record the watch model, Wear OS version, vehicle model/year, and vehicle
  software version.
- Remove VINs, account information, device identifiers, private keys, pairing
  codes, and other personal data from screenshots or logs.
- Keep a physical Tesla key card available while testing.

For a security vulnerability, follow the private-reporting guidance in
[SECURITY.md](SECURITY.md) instead of opening a public issue with sensitive
details.

Installation, enrollment, and recovery behavior is documented in:

- [docs/INSTALL.md](docs/INSTALL.md)
- [docs/ENROLLMENT.md](docs/ENROLLMENT.md)
- [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
- [docs/BUILDING.md](docs/BUILDING.md)

## Development

Use Android Studio's bundled JDK or another compatible Java 17+ runtime:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew testDebugUnitTest assembleDebug lintDebug
./gradlew assembleRelease
```

Before submitting a pull request:

1. Keep Version 1 local-only. Do not add internet, Bluetooth, account, location,
   or storage permissions without an explicit architecture and threat-model
   review.
2. Add or update tests for APDU or cryptographic behavior.
3. Do not log APDUs, public-key exchanges, shared secrets, VINs, or vehicle
   identifiers.
4. Update README, SECURITY, or protocol documentation when behavior or security
   assumptions change.
5. Run unit tests, debug lint, and the release build.

## License

By contributing, you agree that your contribution is licensed under
GPL-3.0-only, consistent with this repository and its upstream project.
