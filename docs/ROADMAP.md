# Roadmap

## Version 1 — manual NFC key card

Current scope:

- local-only Host Card Emulation;
- hardware-backed P-256 credential;
- manual app launch and NFC tap;
- no Tesla account, token, internet, Bluetooth, or backend.

Physical enrollment was successfully validated on July 29, 2026. Lock, unlock,
and drive authorization should still be tested repeatedly on each watch and
vehicle combination before the app is treated as a primary key.

## Version 2 — manual BLE command

Potential work:

- port the authenticated VCSEC session and command framing from Tesla's
  Apache-licensed Vehicle Command SDK;
- implement Android BLE discovery and transport;
- enroll a separate command key;
- expose a Wear OS tile for explicit lock and unlock actions;
- measure latency and battery impact.

Version 2 remains local and should not require a Tesla OAuth token for BLE
vehicle commands.

## Version 3 — passive proximity

Only after Version 2 measurements:

- background scan using the least-privileged supported Wear OS mechanism;
- approach unlock and walk-away lock with explicit user controls;
- false-positive, relay, and battery testing;
- clear degraded-mode behavior when background execution is restricted.

Remote climate, charging, trunk, or internet-routed commands are separate from
the local-key roadmap. If added, tokens should remain off the watch and be held
by a separately secured backend.
