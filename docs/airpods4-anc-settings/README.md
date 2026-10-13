# macOS AirPods Settings — Reverse Engineering Findings

Investigation of how the AirPods settings pane (System Settings on macOS 26 / Darwin 25.5,
Apple Silicon) configures and activates settings for AirPods 4 (ANC), serial L41R6T4YXJ.

Date: 2026-10-13. Method: live system inspection + static analysis (REA/Ghidra) of
HeadphoneSettingsExtension, HeadphoneManager.framework and CoreBluetooth.framework
(extracted from the dyld shared cache), plus string/symbol triage of bluetoothd and
bluetoothuserd.

## Files

| File | Contents |
|------|----------|
| [01-architecture-overview.md](01-architecture-overview.md) | End-to-end chain: UI → model → XPC → bluetoothd → AirPods |
| [02-ui-layer.md](02-ui-layer.md) | The System Settings pane extension |
| [03-write-path.md](03-write-path.md) | CBDeviceSettings and modifyDevice, decompiled |
| [04-transport-device-protocol.md](04-transport-device-protocol.md) | XPC transport, bluetoothd, AACP on-device commands |
| [05-persistence-and-sync.md](05-persistence-and-sync.md) | Local records + iCloud/CloudKit sync |
| [06-setting-field-map.md](06-setting-field-map.md) | Every toggle in the screenshots → field name |
| [07-tooling-notes.md](07-tooling-notes.md) | How to reproduce the analysis |
| [08-evidence-log.md](08-evidence-log.md) | Evidence IDs, commands run, limitations, unknowns |
| [09-porting-to-non-apple-device.md](09-porting-to-non-apple-device.md) | What can/cannot be ported to a non-Apple device |
| [10-daemon-approximations.md](10-daemon-approximations.md) | Rebuilding Conversation Awareness / Personalized Volume as a host daemon |

## TL;DR

The pane is a SwiftUI ExtensionKit appex. Toggles build a private
`CBDeviceSettings` object and call `[CBController modifyDevice:settings:completion:]`
(private CoreBluetooth API). It serializes to XPC and reaches `bluetoothd`, which pushes
the change to the AirPods themselves over AACP (Apple Accessory Protocol) — the settings
live in AirPods firmware; the Mac sends commands. Mac-side state plus cross-device sync
lives in `~/Library/com.apple.bluetooth.services.cloud/CachedRecords/` and is synced by
`bluetoothuserd` over a CloudKit KVS (`com.apple.bluetooth.cloud.settings`).
