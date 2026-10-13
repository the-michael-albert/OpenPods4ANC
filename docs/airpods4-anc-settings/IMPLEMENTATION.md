# OpenPods implementation notes (AirPods 4 ANC)

What the reverse-engineering docs in this folder allowed us to implement:

- **Model detection**: `AirPods4` (beacon id `1920`) and `AirPods4ANC` (`1B20`) in `PodsStatus`.
  These IDs are not from the RE docs and are unverified against real hardware; check with the
  debug log (`parseStatusForLogger`) and correct if needed.
- **Settings model**: `pods/settings/AirPodsSettings` mirrors the settings pane (06-setting-field-map.md),
  including press-and-hold cycling and the host-side vs buds-side split (09, 10).

Not implemented: sending settings to the buds. The AACP encodings were never decoded (08), and newer
features need per-pairing encrypted keys (09). Conversation Awareness / Personalized Volume would be
host-side daemons (10).
