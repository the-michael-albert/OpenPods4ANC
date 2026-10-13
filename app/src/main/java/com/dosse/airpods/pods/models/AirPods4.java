package com.dosse.airpods.pods.models;

import com.dosse.airpods.pods.Pod;

public class AirPods4 extends RegularPods {
    public AirPods4(Pod leftPod, Pod rightPod, Pod casePod) {
        super(leftPod, rightPod, casePod);
    }

    @Override
    public String getModel() {
        return Constants.MODEL_AIRPODS_4;
    }
}
