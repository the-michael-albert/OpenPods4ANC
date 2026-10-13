# 09 — Porting These Settings to a Non-Apple Device

Question: can all settings from the screenshots be applied from a non-Apple device
(Android / Windows / Linux)?

## Short answer

Partially. Standard-Bluetooth features work with any OS. Apple-proprietary features need
Apple's encrypted accessory protocol (AAP/AACP); community clients cover a subset
(ANC/transparency, battery, ear detection). iCloud-synced settings and personalization
cannot be ported at all — they stay inside Apple's ecosystem.

## What the protocol situation is (from this investigation)

- Settings are pushed by bluetoothd to the buds over **AACP** on a BLE service
  (`AACP_MSG_SET_FEATURE_FLAGS` etc.). The message formats are proprietary and, for
  newer features, **encrypted with per-pairing keys**.
- Those keys exist on the paired Mac in `MagicSettings/<MAC>`:
  `accessoryKey`, `encryptionKey`, `irk`, `masterKey`. They are minted at pairing time
  and shared across the owner's devices via the CloudKit KVS
  (`com.apple.bluetooth.cloud.settings`) — a non-Apple device cannot join that store.

## What works from a non-Apple device today

Community reverse engineering of the *unauthenticated* (v1) Apple accessory protocol:

- **Noise control**: cycle/set Off / Transparency / (Noise Cancellation). Adaptive is
  newer and generally unsupported in v1 clients.
- **Read state**: battery per bud + case, in-ear detection status, charging state.
- **Rename** (on some firmware/clients), play/pause, media control via standard AVRCP.

Existing implementations: **OpenPods** (Android, F-Droid), various Python/Go libs
implementing the AAP GATT writes. These require the AirPods connected to that device
over Classic BT (A2DP/HFP) while the GATT service is open.

## What cannot be ported

| Setting | Why not |
|---|---|
| Adaptive Audio + noise slider | needs AAP v2 encrypted commands |
| Conversation Awareness | Apple-device-side ML (mics + host processing); not a buds-only feature |
| Personalized Volume | host-side feature (Apple devices only) |
| Personalized Spatial Audio | requires Apple head-scan profile + host renderer |
| Press-and-Hold remapping, call-control mapping, Head Gestures, Mic mode | configured via encrypted AAP v2 (`CBDeviceSettings` fields → `AACP_MSG_SET_FEATURE_FLAGS`) |
| "Connect to This Mac" / auto-switching | Apple Continuity + iCloud; no third-party equivalent |
| Find My Network | Apple ecosystem |

## If you wanted the full set anyway (research path)

1. Extract pairing keys from the Mac: `MagicSettings/40:B3:FA:95:7E:94`
   (`accessoryKey`/`encryptionKey`/`irk`).
2. Reimplement the AAP v2 client: BLE GATT service on the AirPods, message framing
   observed in bluetoothd's AACP parser (`AACP_MSG_*`), encryption from the keys above.
   Starting point: decompile `AACP_MSG_SET_FEATURE_FLAGS` handling in bluetoothd
   (`strings`/nm entry points listed in 08).
3. Map each screenshot toggle to a `CBDeviceSettings` field (see 06) and find its AACP
   command encoding.
4. Remaining blockers even then: features whose *processing* runs on Apple hosts
   (Conversation Awareness, Personalized Volume, Personalized Spatial Audio) are
   physically impossible from a non-Apple host — the buds only expose the hooks.

Caveat: command bytes per feature were **not** decoded in this investigation; step 2
is the open work. Also note AAP v2 keys are per-pairing — re-pairing to the non-Apple
device rotates them and the Mac's copies become stale.
