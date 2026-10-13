# 02 — UI Layer: HeadphoneSettingsExtension

## Location and identity

- Path: `/System/Library/ExtensionKit/Extensions/HeadphoneSettingsExtension.appex`
- Binary: universal x86_64 + arm64e, SHA-256 (x86_64 slice) `01c39716fc32013cbd7afa803159ac0f6ae377d3f750b043211a46ea57363438`
- Frameworks linked (otool -L): `HeadphoneManager.framework` (private), `CoreBluetooth`,
  `Settings.framework` (private), `HearingModeSettingsUIMacOS` (private), SwiftUI, ExtensionKit.

## What it contains

- SwiftUI views for the pane sections seen in the screenshots (listening mode,
  press and hold, call controls, audio, head gestures, spatial audio, Find My).
- Swift module `HeadphoneSettingsExtension`, with classes:
  - `HeadphoneManagerOriginal` — bridge to `HeadphoneManager.framework`
  - `HeadphoneSettingsManager` — selected-device state (`selectedHPDevice`)
  - `HeadphoneExtension.ExtensionService` — ExtensionKit service host
- Relevant strings: `CBListeningMode`, `AAHeadGestureConfig`, `macOSHeadphoneViewInEarDetection`,
  `macOSHeadphoneViewOffListeningMode`, `macOSHeadphoneViewHeadGesture`,
  `listeningModeOffAllowed`, `endCallCapability`.

## Related extensions found on disk

- `Bluetooth.appex`, `Sound.appex`, `AudioAppIntentsExtension.appex`,
  `BluetoothSettingsAppIntentsWidgetExtension.appex` — siblings in
  `/System/Library/ExtensionKit/Extensions/`.

## Notes

- The extension has almost no business logic: it renders state from `HeadphoneManager`
  and forwards changes as a `CBDeviceSettings` (see 03).
- `HeadphoneManager.framework` is **not on disk** — it lives inside the dyld shared cache
  (see 07-tooling-notes.md for extraction).
