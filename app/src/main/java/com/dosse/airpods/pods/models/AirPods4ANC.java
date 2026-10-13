package com.dosse.airpods.pods.models;

import com.dosse.airpods.pods.Pod;
import com.dosse.airpods.pods.settings.AirPodsSettings;

/**
 * AirPods 4 (ANC), model A3055. Same beacon layout as the other AirPods; additionally
 * exposes the settings model described in docs/airpods4-anc-settings.
 */
public class AirPods4ANC extends AirPods4 {
    public AirPods4ANC(Pod leftPod, Pod rightPod, Pod casePod) {
        super(leftPod, rightPod, casePod);
    }

    @Override
    public String getModel() {
        return Constants.MODEL_AIRPODS_4_ANC;
    }

    /** @return a settings object populated with the documented defaults for this model */
    public AirPodsSettings newSettings() {
        return AirPodsSettings.forAirPods4ANC();
    }
}
