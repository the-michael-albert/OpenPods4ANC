# 10 — Conversation Awareness & Personalized Volume via a Host Daemon

Can the two "host-side" Apple features be reproduced on a non-Apple device with a
custom daemon? Short answer: yes as approximations. You rebuild the logic as OS-level
volume automation; nothing touches the buds' firmware.

## Conversation Awareness daemon

Apple's version: buds detect the wearer's own voice (mics + bone-conduction sensor),
report it, host ducks media volume, buds retune their mics/noise. The own-voice signal
is not accessible to third parties, but the approximation works:

- Run voice-activity detection (VAD) on the mic uplink while the AirPods are connected.
- VAD fires → duck media volume (e.g. to a configurable floor). VAD silent for a decay
  window → restore previous volume.

Constraints:

- **Classic Bluetooth profile problem**: opening the mic forces the A2DP → HFP switch,
  which degrades playback to call quality. So the VAD path only runs while something
  already owns the mic (e.g. during a call), or you accept the switch.
- **LE Audio fixes this**: AirPods 4 (ANC) support LE Audio. With LE Audio the mic
  (LC3) and high-quality playback run simultaneously, so a Conversation Awareness
  daemon is actually viable on Linux with PipeWire. macOS/Windows do not expose this
  cleanly against AirPods.

## Personalized Volume daemon

- Periodically sample ambient noise, map noise level to a volume curve (feedback loop).
- Ambient estimate options:
  - Host machine's own mic — wrong location (desk vs ear) but cheap and always available.
  - Briefly opening the bud mic — better placement, but hits the same Classic BT
    profile-switch problem; fine over LE Audio.
- This one is genuinely tractable: a sensor you already have plus a curve.

## What the daemon loses vs Apple

| Gap | Reason |
|---|---|
| Own-voice discrimination | Apple uses in-ear bone conduction + mic arrays; a plain VAD triggers on any speech the mic hears (other people, nearby TV). Expect more false positives. |
| Mic/ANC retuning during conversation | That half runs in Apple firmware; unreachable. |
| Follows the buds across devices | Apple's version syncs via iCloud; the daemon only acts while the buds are connected to its own host. |

## Implementation sketch (LE Audio / Linux + PipeWire)

1. Watch for AirPods connect (BlueZ device events).
2. Ensure LE Audio path (LC3 bidirectional) so uplink capture never forces an A2DP↔HFP switch.
3. Uplink stream → lightweight VAD (e.g. WebRTC VAD or silero) with hangover/decay.
4. Duck/restore media volume via PipeWire node volumes; clamp to user-configured floor.
5. Optional Personalized Volume: secondary loop sampling ambient noise (host mic),
   mapping SPL → volume offset, merged with the VAD duck.

Neither feature communicates with the AirPods protocol at all — the buds just behave
as a normal audio device; all logic lives in the daemon.
