package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ReservationStatusSubscribeChannel extends SubscribeChannelRegistry {
    private static final String TOPIC = "/reservation/+";
    private static final String TOPIC_WITH_RESERVATION_ID = "/reservation/%d";
    private static final String KEY_EXPIRED = "expired";
    private static final String KEY_TRANSACTION = "transaction";

    private final int mReservationId;

    public ReservationStatusSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
        mReservationId = -1;
    }

    public ReservationStatusSubscribeChannel(INetworkConnection networkConnection, int reservationId) {
        super(networkConnection);
        mReservationId = reservationId;
    }

    @Override
    public Uri getChannelDescription() {
        if (mReservationId != -1) {
            return new MqttUri(StringFormatter.format(TOPIC_WITH_RESERVATION_ID, mReservationId).getValue());
        }
        return new MqttUri(TOPIC);
    }
}
