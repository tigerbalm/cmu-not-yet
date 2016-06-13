package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ReservationResponseChannel extends ServerChannelRegistry {
    private final static String mTopic = "/facility/+/reservation/#";

    public ReservationResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(mTopic);
    }

    public static final int getFacilityId(Uri uri) {
        return Integer.parseInt((String) uri.getPathSegments().get(2));
    }

    public static final String getSessionKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("session_key").asString();
    }

    public static final long getReservationTimestamp(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("reservation_ts").asLong();
    }
}
