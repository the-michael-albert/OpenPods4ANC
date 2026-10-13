# 04 — Transport and On-Device Protocol

## XPC leg: app → bluetoothd

- `CBController` (private CoreBluetooth) is the XPC client. Service name observed in
  CoreBluetooth strings: **`com.apple.bluetooth.xpc`** (user side also has
  `com.apple.bluetoothuser.xpc` for bluetoothuserd).
- `CBDeviceSettings` crosses the boundary via `encodeWithXPCObject:` /
  `initWithXPCObject:`.
- Server: `/usr/sbin/bluetoothd` (universal x86_64 + arm64e, "BluetoothDaemon" strings).

## Device leg: bluetoothd → AirPods (AACP)

The AirPods expose **AACP** (Apple Accessory Protocol) as a Bluetooth service
(`system_profiler SPBluetoothDataType`: `Services: 0x980019 < HFP AVRCP A2DP AACP GATT ACL >`).
bluetoothd contains the client implementation. Message names found in the binary
(`strings | grep -oE 'AACP_MSG_[A-Z_]+' | sort -u`):

```
AACP_MSG_CAPABILITIES_RESP      AACP_MSG_GET_CERTIFICATES_RESP
AACP_MSG_DELETE_KEYS            AACP_MSG_GYRO_INFO
AACP_MSG_DEV_LIST               AACP_MSG_RELAY_MSG_INDICATION
AACP_MSG_EA_SESSION_DOWNLOAD    AACP_MSG_SET_FEATURE_FLAGS
AACP_MSG_GAPA                   AACP_MSG_TIPI_TABLE
```

- `AACP_MSG_SET_FEATURE_FLAGS` is the family that carries feature/setting changes to
  accessory firmware (e.g. noise-control and gesture behavior).
- Log fragments show the AACP channel lifecycle: "AACP Connection attempt by device",
  "AACP Client", capability negotiation ("AACP Config Jitter Buffer ... supported").

## Interpretation

- Settings shown in the pane (listening mode, press-and-hold mapping, ear detection,
  conversation awareness...) are **applied by the AirPods firmware**. The Mac's role is
  to negotiate capabilities and push new values over AACP.
- Audio-path settings (adaptive/transparency/ANC state) additionally interact with the
  audio stack (`bluetoothaudiod`, `BluetoothAudio.framework`), but the *configuration*
  itself travels via bluetoothd/AACP.
