# 01 — Architecture Overview

Verified end-to-end chain for configuring an AirPods setting from System Settings:

```
System Settings (host app)
  └─ HeadphoneSettingsExtension.appex      SwiftUI pane (ExtensionKit)
       └─ HeadphoneManager.framework       Swift model, per-setting @Published props
            └─ CBDeviceSettings            private CoreBluetooth settings object
                 └─ [CBController modifyDevice:settings:completion:]
                      └─ encodeWithXPCObject: → XPC (com.apple.bluetooth.xpc)
                           └─ bluetoothd (BluetoothDaemon, /usr/sbin/bluetoothd)
                                └─ AACP messages over BLE/GATT to the AirPods
                                     └─ AirPods firmware stores + applies the setting

Side channel (persistence + cross-device sync):
  bluetoothuserd (/usr/libexec/bluetoothuserd)
    └─ ~/Library/com.apple.bluetooth.services.cloud/CachedRecords/*.plist  (local cache)
    └─ CloudKit KVS "com.apple.bluetooth.cloud.settings"                   (sync)
```

Key facts:

- The settings are **not** stored in `defaults` / user preference domains. Searched
  `com.apple.Bluetooth`, `com.apple.bluetoothuserd`, `com.apple.AudioAccessory`,
  `com.apple.BluetoothAudioAgent` — none hold per-setting values (ear detection,
  press-and-hold, listening mode, etc.). Only ancillary data (last battery levels,
  now-playing target address).
- The authoritative copy of most settings is **in the AirPods firmware**. The Mac sends
  protocol commands (AACP) and keeps a synced mirror for the UI and for other devices.
- The AACP service is visible on the connected device: `system_profiler SPBluetoothDataType`
  shows `Services: 0x980019 < HFP AVRCP A2DP AACP GATT ACL >` for the AirPods.
- Cloud sync explains why AirPods settings roam between iPhone / iPad / Mac.
