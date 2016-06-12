package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ReservationRequestChannel extends ClientChannelRegistry {

    private final static String mTopic = "/facility/%d/reservation";

    private int mFacilityId = 0;

    private ReservationRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public ReservationRequestChannel setFacilityId(int facilityId) {
        mFacilityId = facilityId;
        return this;
    }

    public static ReservationRequestChannel build(INetworkConnection networkConnection, int facilityId) {
        return new ReservationRequestChannel(networkConnection).setFacilityId(facilityId);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(mTopic, mFacilityId).getValue());
    }
}
