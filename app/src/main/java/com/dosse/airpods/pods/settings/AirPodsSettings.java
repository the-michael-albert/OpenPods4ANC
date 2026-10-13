package com.dosse.airpods.pods.settings;

import java.util.EnumSet;
import java.util.Set;

/**
 * In-memory model of the AirPods 4 (ANC) settings pane, see docs/airpods4-anc-settings
 * (06-setting-field-map.md). Each field notes the macOS CBDeviceSettings/HeadphoneManager
 * property it corresponds to.
 * <p>
 * This class only models state. The AACP wire encoding of each setting was not decoded by
 * the reverse engineering work (08-evidence-log.md), so nothing here is sent to the buds.
 * Conversation Awareness, Personalized Volume and Personalized Spatial Audio are host-side
 * features (09/10) and are therefore host-driven toggles, not buds commands.
 */
public class AirPodsSettings {
    /** CBDeviceSettings.listeningMode */
    public enum ListeningMode { OFF, TRANSPARENCY, ADAPTIVE, NOISE_CANCELLATION }

    /** CBDeviceSettings.clickHoldModeLeft / clickHoldModeRight */
    public enum PressAndHoldAction { LISTENING_MODE }

    /** CBDeviceSettings.muteControlConfig / endCallConfig */
    public enum CallGesture { PRESS_ONCE, PRESS_TWICE }

    /** smartRoutingMode / allowsAutoRoute family ("Connect to This Mac") */
    public enum ConnectMode { AUTOMATICALLY, WHEN_LAST_CONNECTED }

    /** Microphone row: "Automatically Switch AirPods" etc. */
    public enum MicrophoneMode { AUTOMATIC, ALWAYS_LEFT, ALWAYS_RIGHT }

    /** Name (CBDeviceSettings.name) */
    public String name = "AirPods";

    public ListeningMode listeningMode = ListeningMode.TRANSPARENCY;

    public PressAndHoldAction pressAndHoldLeft = PressAndHoldAction.LISTENING_MODE;
    public PressAndHoldAction pressAndHoldRight = PressAndHoldAction.LISTENING_MODE;

    /** Modes included in the press-and-hold cycle (listeningModeConfigs / allowNoiseManagementOff) */
    public final Set<ListeningMode> pressAndHoldCycle =
            EnumSet.of(ListeningMode.TRANSPARENCY, ListeningMode.NOISE_CANCELLATION);

    /** Answer Call is fixed to press-once and not user-writable. */
    public static final CallGesture ANSWER_CALL = CallGesture.PRESS_ONCE;
    public CallGesture muteUnmute = CallGesture.PRESS_ONCE;
    public CallGesture endCall = CallGesture.PRESS_TWICE;

    /** adaptiveVolumeConfig; host-side */
    public boolean personalizedVolume = false;
    /** conversationDetectConfig; host-side */
    public boolean conversationAwareness = true;
    /** Adaptive Audio slider, 0 = less noise .. 100 = more noise (smartRoutingMode / adaptiveVolumeConfig tuning) */
    public int adaptiveAudioNoise = 50;
    /** inEarDetectionEnabled */
    public boolean automaticEarDetection = true;
    public ConnectMode connectMode = ConnectMode.AUTOMATICALLY;

    /** "Pause Media When Falling Asleep" */
    public boolean pauseMediaWhenFallingAsleep = false;
    public MicrophoneMode microphone = MicrophoneMode.AUTOMATIC;

    /** AADeviceRecord.headGestureMode (2 observed = off) */
    public boolean headGestures = false;
    public boolean chargingCaseSounds = true;
    /** Find My network; Apple ecosystem, read-only here */
    public boolean findMyNetwork = true;

    /** About section, read-only */
    public String modelName = "AirPods 4 (ANC)";
    public String modelNumber = "A3055";

    public static AirPodsSettings forAirPods4ANC() {
        return new AirPodsSettings();
    }

    /** Cycles to the next listening mode in {@link #pressAndHoldCycle}, as press-and-hold does. */
    public ListeningMode nextListeningMode() {
        ListeningMode[] all = ListeningMode.values();
        for (int i = 1; i <= all.length; i++) {
            ListeningMode candidate = all[(listeningMode.ordinal() + i) % all.length];
            if (pressAndHoldCycle.contains(candidate)) {
                return candidate;
            }
        }
        return listeningMode;
    }

    /** Settings that can only be implemented host-side on non-Apple devices (docs 09/10). */
    public static boolean isHostSide(String setting) {
        return "conversationAwareness".equals(setting) || "personalizedVolume".equals(setting);
    }
}
