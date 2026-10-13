# 03 — Write Path: from toggle to CBDeviceSettings

## Model layer (HeadphoneManager.framework, in shared cache)

Swift classes `HeadphoneDevice` / `AADevice` expose per-setting properties as Combine
`@Published` (observed via exported symbol names):

- `listeningMode: CBListeningMode`
- `pressHoldOff: Bool`
- `inEarDetection: Bool`
- `endCallGesture: CBPeerConfig`
- `headGestureToggle: AAMultiState`
- `declineDismissSkipConfig: AAHeadGestureConfig`
- `acceptReplyPlayPauseConfig: AAHeadGestureConfig`
- `isUpdatingDeviceConfig: Bool`
- `btAddress`, `cbDevice` — link to the CoreBluetooth device object

(The `CB*` prefix = private CoreBluetooth types; the `AA*` prefix = AudioAccessory types.)

## ObjC bridge in the extension

`-[HeadphoneManagerOriginal updateDeviceConfigWithHpDevice:settings:]` (decompiled, Ghidra,
address 0x100013520 in the x86_64 slice):

1. Logs `settings.description` via `Settings::Log::notice`.
2. Sets `HeadphoneDevice.isUpdatingDeviceConfig = true`.
3. Reads `hpDevice.btAddress` for logging.
4. Takes `hpDevice.cbDevice` (a private `CBDevice`) and calls
   **`[self modifyDevice:cbDevice settings:settings completion:<block>]`**.

## CBDeviceSettings (private CoreBluetooth)

Defined in `CoreBluetooth.framework` (inside the shared cache). Observed fields
(nm symbol list, getters and setters):

```
aclLinkState                  listeningMode               setListeningModeConfigs:
aclPriority                   listeningModeConfigs        setMicrophoneMode:
adaptiveVolumeConfig          microphoneMode              setMuteControlConfig:
allowsAutoRoute               muteControlConfig           setName:
audioRouteHidden              name                        setPlacementMode:
clickHoldModeLeft             placementMode               setPowerSourceMock:
clickHoldModeRight            powerSourceMock             setRelinquishAudioRoute:
conversationDetectConfig      relinquishAudioRoute        setSelectiveSpeechListeningConfig:
crownRotationDirection        selectiveSpeechListeningConfig  setSmartRoutingMode:
deviceFlagsMask               smartRoutingMode            setSpatialAudioAllowed:
deviceFlagsValue              spatialAudioAllowed         setSpatialAudioMode:
doubleTapActionLeft           spatialAudioMode
doubleTapActionRight          endCallConfig
```

Serialization: `-[CBDeviceSettings encodeWithXPCObject:]` /
`-[CBDeviceSettings initWithXPCObject:error:]` — the object is built for XPC transport
(see 04).

`-[CBController modifyDevice:settings:completion:]` at 0x7ff81d0fe3e0 in CoreBluetooth;
variants: `modifyDevice:completion:`,
`modifyDevice:connectionPriorityDevices:timeoutSeconds:completionHandler:`,
`modifyDevice:peerSourceDevice:peerSourceState:requestFlags:completionHandler:`.
