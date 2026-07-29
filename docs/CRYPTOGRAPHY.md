# Cryptography and protocol audit

## Credential

Tesla key cards authenticate with a stable NIST P-256 (`secp256r1`) key pair.
TeslaKey generates that pair under alias `tesla_key_card_v1` in Android
Keystore with only the key-agreement purpose enabled.

StrongBox generation is attempted first when the watch advertises StrongBox.
If that provider is unavailable, the app retries in the normal Android
Keystore, then verifies that Android reports either StrongBox, Trusted
Environment, or unknown-secure enforcement. Software-only enforcement is
rejected.

## APDU subset

TeslaKey supports the minimal commands needed for enrollment and use:

| Command | APDU | Response |
| --- | --- | --- |
| Select card AID | `00 A4 04 00` + `7465736c614c6f676963` | `90 00` |
| Select phone-key AID | `00 A4 04 00` + `f465736c614c6f676963` | `90 00` |
| Get public key | `80 04 00 00` | uncompressed P-256 point + `90 00` |
| Authenticate | `80 11 00 00` + 81-byte body | 16-byte response + `90 00` |
| Get form factor | `80 14 00 00` | `00 01 90 00` |

The authenticate body contains a 65-byte uncompressed vehicle public key and a
16-byte challenge.

## Authentication calculation

1. Validate that the vehicle public key is a P-256 point.
2. Calculate the ECDH shared secret using the non-exportable watch private key.
3. Hash the shared secret with SHA-1.
4. Use the first 16 hash bytes as an AES-128 key.
5. Replace the first four challenge bytes with fresh random bytes.
6. Encrypt the 16-byte challenge using one AES block with no padding.

The use of SHA-1 and AES-ECB here is protocol compatibility, not a general
cryptographic design recommendation. SHA-1 is used as a key-derivation step,
not as a collision-resistant signature. ECB processes exactly one block, so it
does not expose repeated-block patterns.

Temporary shared-secret, digest, AES-key, and challenge arrays are overwritten
after each response on a best-effort basis. The Java/Android runtime may still
make copies outside application control.

## Differences from upstream

The audited upstream revision was
`aa9866bcbbf4a5de6e9212bbfa661eabfa060030`.

Upstream generated the P-256 scalar with Bouncy Castle in app memory, encrypted
that scalar with an RSA Android Keystore key, and stored the ciphertext in
preferences. This fork:

- generates the P-256 credential directly inside Android Keystore;
- removes the Bouncy Castle and Commons Codec dependencies;
- validates the vehicle curve point explicitly;
- replaces the copied `javax.smartcardio` parser with a small strict parser;
- catches malformed input instead of allowing parser exceptions to terminate
  the HCE service;
- verifies P1/P2, exact body lengths, and selected AIDs;
- disables backup and requires hardware-backed key storage;
- adds deterministic unit tests for APDU routing and the challenge response.

## Evidence boundary

Unit tests verify parsing, command routing, response formatting, and the
ECDH-derived SHA-1/AES calculation with a deterministic credential. Android
Keystore provider behavior requires an API 31+ physical watch or compatible
instrumented device. NFC timing, AID routing, enrollment, and vehicle behavior
require an actual supported watch and vehicle.
