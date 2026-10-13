# 08 — Evidence Log

## Live system observations (2026-10-13)

| Observation | Command |
|---|---|
| AirPods 4 (ANC) connected, serial L41R6T4YXJ, services `< HFP AVRCP A2DP AACP GATT ACL >` | `system_profiler SPBluetoothDataType` |
| Settings not in defaults domains | `defaults read` across com.apple.Bluetooth*, com.apple.AudioAccessory, com.apple.BluetoothAudioAgent |
| CloudKit account cache in bluetoothuserd prefs | `defaults read com.apple.bluetoothuserd` |
| Record store + AirPods records | `plutil -p ~/Library/com.apple.bluetooth.services.cloud/CachedRecords/{MagicSettings,AADeviceRecord,DeviceRecord}/40:B3:FA:95:7E:94` (and siblings) |
| `DeviceRecord.lastModifiedDate` = 2026-10-13 16:39:10 UTC — screenshot time | plutil output |
| headGestureMode=2 in AADeviceRecord; Head Gestures toggle off in screenshot | plutil output + screenshot |
| Cloud sync container `com.apple.bluetooth.cloud.settings` | `strings bluetoothuserd.x86_64` |
| AACP message names in bluetoothd | `strings bluetoothd.x86_64 | grep AACP_MSG_` |
| CBDeviceSettings field list + modifyDevice selectors | `nm CoreBluetooth (extracted)` |

## Static analysis (REA / Ghidra 12.1.4)

| Finding | Evidence ID |
|---|---|
| Extension ObjC/Swift class list (HeadphoneManagerOriginal, HeadphoneSettingsManager) | nm on x86_64 slice (local symbols) |
| Setting-key strings (CBListeningMode, AAHeadGestureConfig, macOSHeadphoneView*) | ev_24f19dab… (search_strings on HeadphoneSettingsExtension.x86_64, sha256 01c39716…22bf) |
| `updateDeviceConfigWithHpDevice:settings:` → `modifyDevice:settings:completion:` decompile | ev_7320e4a6… (procedure_pseudo_code 0x100013520) |
| HeadphoneManager symbols (listeningMode, pressHoldOff, inEarDetection, headGestureToggle, isUpdatingDeviceConfig, …) | `dyld_info -exports` on shared-cache member |
| `CBDeviceSettings` class + full getter/setter list | nm on extracted CoreBluetooth.framework |

## Limitations / unknowns

- Exact AACP command encodings per setting not decoded (bluetoothd AACP parser not
  decompiled). bluetoothd is 12 MB; symbol names available if needed.
- Enum values for listeningMode / smartRoutingMode not decoded.
- `cloudMetadata` nested plists (CloudKit record metadata) not unpacked.
- Root-side bluetoothd state (`/Library/Application Support/BTServer/...`,
  /Library/Preferences) not read — requires sudo.
- Ghidra analysis of HeadphoneManager.framework timed out (2.4 MB Swift binary);
  conclusions from that framework rest on exported symbol names, not decompiled bodies.
- Extracted cache copy at `/tmp/rea-airpods/dsc` (5 GB) — safe to delete.
