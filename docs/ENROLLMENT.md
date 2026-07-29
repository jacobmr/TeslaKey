# Enrolling and using the watch

TeslaKey behaves like an additional NFC key card. It is not a Bluetooth phone
key and does not provide passive entry.

## Safety checklist

Before enrollment:

- park the vehicle in a safe location;
- keep an authorized physical Tesla key card with you;
- make sure the watch has enough battery;
- confirm the watch has a PIN, pattern, or other lock configured;
- open TeslaKey and wait for **Ready — keep this screen open**; and
- confirm the reported storage is **StrongBox** or trusted hardware (**TEE**).

Do not continue if TeslaKey reports unsupported card emulation,
software-backed storage, or key-creation failure.

## Add TeslaKey from the vehicle touchscreen

Tesla's labels can change slightly between vehicle software versions.

1. Sit in the parked vehicle with the authorized physical key card.
2. On the touchscreen, open
   **Controls → Locks → Keys → Add Key**.
3. Unlock the watch.
4. Open TeslaKey and keep its screen awake.
5. Hold the watch against the center-console NFC card reader shown by the
   vehicle.
6. Keep the watch steady until the vehicle recognizes the new key and prompts
   for confirmation.
7. Scan the already-authorized **physical key card** on the reader.
8. Wait for the new entry to appear in the vehicle's key list.
9. Select the pencil icon and rename it to something recognizable, such as
   **TeslaKey Watch**.

Tesla's official [Model Y key instructions](https://www.tesla.com/ownersmanual/modely/en_us/GUID-E004FAB7-1C71-448F-9492-CACF301304D2.html)
describe the same new-key-then-existing-key confirmation sequence.

## Finding the console reader

Follow the vehicle's on-screen picture first. Reader position depends on model,
console design, and production date.

- Some Model 3/Y vehicles read key cards behind the cup holders on the top of
  the center console.
- Newer vehicles can use either wireless phone charger.
- Other compatible vehicles may place the reader differently.

Tesla's current
[Model Y service guidance](https://service.tesla.com/docs/Public/diy/modely/en_us/GUID-67E192C9-4308-44B7-899F-65344F487F1B.html)
notes that vehicles built before approximately February 2025 generally use the
reader behind the cup holders, while later vehicles generally use a wireless
phone charger. Check the owner's manual for the exact vehicle.

The NFC antenna also varies by watch. Try:

1. the watch face flat against the reader;
2. the side of the watch body nearest the NFC antenna; and
3. slowly moving the watch a few centimeters across the indicated reader area.

Keep the watch close to the surface for a moment at each position. A quick wave
can miss the exchange.

## Verify before relying on it

Keep the physical key card outside the vehicle or in your hand during these
tests so you cannot lock it inside.

### Test locking

1. Close all doors.
2. Open TeslaKey and keep the watch unlocked.
3. Hold the watch against the NFC reader on the driver's B-pillar, just below
   the side camera on Model 3/Y.
4. Confirm that the vehicle locks.

### Test unlocking

1. Open TeslaKey and keep the watch unlocked.
2. Hold it against the same B-pillar reader.
3. Confirm that the vehicle unlocks.

### Test drive authorization

1. Sit in the driver's seat.
2. Open TeslaKey and keep the watch unlocked.
3. Hold the watch against the console NFC reader.
4. After the vehicle accepts it, press the brake pedal and select a drive mode.

Tesla key-card authorization is time-limited. If too much time passes, scan the
watch again.

Repeat all three tests several times before treating the watch as a dependable
backup key. Test again after a watch OS update, Tesla software update, or
TeslaKey app update.

## Everyday operation

### Lock or unlock

1. Wake and unlock the watch.
2. Open TeslaKey.
3. Wait for **Ready — keep this screen open**.
4. Hold the watch against the driver's B-pillar reader until the vehicle
   responds.

### Authorize driving

1. Open TeslaKey in the vehicle.
2. Hold the watch at the console card reader.
3. Press the brake within the authorization window.

TeslaKey does not run a passive background key. The app must be open.

## Require unlocked watch

**Require unlocked watch** is enabled by default and should normally remain
enabled. When enabled, TeslaKey refuses NFC commands while Android reports that
the watch is locked.

If Android Secure NFC is enabled, TeslaKey forces the unlocked-watch requirement
even if the app switch appears off.

Treat an unlocked watch like a physical vehicle key.

## Remove the watch from the vehicle

Remove access promptly if the watch is lost, sold, reset, or no longer trusted:

1. On the vehicle touchscreen, open **Controls → Locks → Keys**.
2. Find the entry you named for TeslaKey.
3. Select its trash icon.
4. Scan an authorized physical key card when the vehicle asks for
   confirmation.
5. Confirm that the old entry disappears.

Do this before uninstalling TeslaKey when possible.

## Reinstalling or resetting

Clearing app storage, uninstalling TeslaKey, or factory-resetting the watch
destroys its non-exportable credential. The old vehicle entry will never work
again.

After a reset:

1. remove the stale key from the vehicle;
2. install TeslaKey;
3. open it to create a new hardware-backed credential; and
4. enroll the new credential as a new key.
