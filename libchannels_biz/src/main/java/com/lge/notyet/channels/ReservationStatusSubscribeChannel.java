package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ReservationStatusSubscribeChannel extends SubscribeChannelRegistry {
    private static final String TOPIC = "/reservation/+";
    private static final String KEY_EXPIRED = "expired";
    private static final String KEY_TRANSACTION = "transaction";

    public ReservationStatusSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static boolean isExpired(NetworkMessage networkMessage) {
        JsonObject object = (JsonObject) networkMessage.getMessage();
        return object.get(KEY_EXPIRED) != null && !object.get(KEY_EXPIRED).isNull() && object.get(KEY_EXPIRED).asInt() == 0;
    }
}
