# 06 — Setting → Field Map (screenshots → internals)

Screenshot toggle/row → observed field or record key. Sources: nm symbol list of
`CBDeviceSettings` (CoreBluetooth.framework), HeadphoneManager.framework exported
properties, and on-disk record dumps.

## Pane: listening mode / press and hold

| UI element | Internal field |
|---|---|
| Listening Mode (Transparency/Adaptive/ANC/Off) | `CBDeviceSettings.listeningMode` (+ `listeningModeConfigs`), Swift `HeadphoneDevice.listeningMode: CBListeningMode` |
| Press and Hold L / R | `CBDeviceSettings.clickHoldModeLeft` / `clickHoldModeRight` |
| Press and Hold to Cycle Between (Off/Transparency/Adaptive/ANC checkboxes) | allowed-mode bit set inside `listeningModeConfigs`; `allowNoiseManagementOff` in `AADeviceRecord`; `listeningModeOffAllowed` / `hideOffListeningModeCapability` (HeadphoneManager capability props) |

## Pane: Call Controls

| UI element | Internal field |
|---|---|
| Answer Call (Press Once) | fixed capability (`endCallCapability` area; not user-writable here) |
| Mute & Unmute | `CBDeviceSettings.muteControlConfig` |
| End Call (Press Twice) | `CBDeviceSettings.endCallConfig`, Swift `endCallGesture: CBPeerConfig` |

## Pane: Audio

| UI element | Internal field |
|---|---|
| Personalized Volume | `CBDeviceSettings.adaptiveVolumeConfig` |
| Conversation Awareness | `CBDeviceSettings.conversationDetectConfig` (+ `selectiveSpeechListeningConfig`) |
| Adaptive Audio slider (Less/More noise) | `CBDeviceSettings.smartRoutingMode` / `adaptiveVolumeConfig` tuning values |
| Automatic Ear Detection | `inEarDetectionEnabled` (CoreBluetooth), `inEarDetection` (HeadphoneDevice) |
| Connect to This Mac (Automatically / When Last Connected) | `CBDeviceSettings.smartRoutingMode` / `allowsAutoRoute` family |

## Pane: Head Gestures / Case

| UI element | Internal field |
|---|---|
| Head Gestures toggle | `AADeviceRecord.headGestureMode` (observed 2 = off), Swift `headGestureToggle: AAMultiState` |
| Enable Charging Case Sounds | case-related flag (AudioAccessoryServices / `AAMultiState` family; exact key not individually observed) |

## Pane: Spatial Audio / Find My / About

| UI element | Internal field |
|---|---|
| Personalized Spatial Audio | `CBDeviceSettings.spatialAudioMode` + `spatialAudioAllowed` |
| Find My Network | FindMyBluetooth.framework / iCloud, not in CBDeviceSettings |
| Name | `CBDeviceSettings.name` + `DeviceRecord/<MAC>.nickname` |
| Model/Serial/Version | read-only from device via AACP certificates + `DeviceRecord` |

Exact enum encodings (e.g. what number = "Adaptive") were not decoded; fields were
verified by name and by correlation with record values (headGestureMode=2 with the
toggle off in the screenshot).
