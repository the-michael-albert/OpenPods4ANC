# 05 — Persistence and Cross-Device Sync

## Local store (per user)

`~/Library/com.apple.bluetooth.services.cloud/`

```
CachedRecords/
  AADeviceRecord/<BT-MAC>        per-accessory setting state
  AAProxCardsRecord/<BT-MAC>
  AccessoryDatabase/…
  DeviceRecord/<BT-MAC>          friendly name, model, identifiers
  DeviceSupportInformationRecord/
  HMDeviceCloudRecord/
  MagicSettings/<BT-MAC>         "Magic" accessory record (AirPods/keys/feature bits)
  MasterKey/
CKTokens/                        CloudKit change tokens
DeferredRecords/                 pending uploads
```

All files are binary plists keyed by Bluetooth MAC address.

## Record contents observed (AirPods 4 ANC, 40:B3:FA:95:7E:94)

- `MagicSettings/40:B3:FA:95:7E:94`: `accessoryKey`, `encryptionKey`, `irk`,
  `masterKey`/`masterHint`, `deviceIDFeatureBitsV1/V2`, `buttonModes`, `settingsMask`,
  `listeningServices`, `productID` (8219 = 0x201B), `cloudMetadata` (nested plist,
  CloudKit record metadata), `ratchet` (record version), `lastModifiedDate`.
- `AADeviceRecord/40:B3:FA:95:7E:94`: `headGestureMode` (2 = off — matches screenshot),
  `declineDismissSkipConfig`, `acceptReplyPlayPauseConfig`, `allowNoiseManagementOff`,
  `chargingReminderEnabled`, `heartRateMonitorCapability`, `cameraControlConfig`,
  `healthKitDataWriteAllowed`, `cloudMetadata`.
- `DeviceRecord/40:B3:FA:95:7E:94`: nickname "Michael's AirPods", model A3055.
  `lastModifiedDate` 2026-10-13 16:39:10 UTC = 09:39 local — exactly the screenshot time;
  touching the settings pane updates these records.

## Sync daemon: bluetoothuserd

- `/usr/libexec/bluetoothuserd` (per-user daemon).
- Strings show a CloudKit KVS + PCS stack: container identifier
  **`com.apple.bluetooth.cloud.settings`**, `CKContainer`, zone change fetch, APS push,
  `com.apple.bluetoothuser.settingsChanged`, "Cloud defaults update ... synchronized",
  "PCS Keys ... not yet synced".
- Local CloudKit layout: `~/Library/com.apple.bluetoothuser/Production/<account>/CKTokens/`
  and `CloudPairedDeviceRecords/`.

## Interpretation

- This KVS is why AirPods settings roam between a user's devices (iPhone/Mac/iPad):
  each device writes changes both to the buds (AACP) and to the cloud record; other
  devices pick the record up and reconcile.
- Battery-level bits live separately in `com.apple.AudioAccessory` defaults
  (`lastSeenBatteryInfosV2`, `lowBatteryDevices`) — ancillary, not settings.
