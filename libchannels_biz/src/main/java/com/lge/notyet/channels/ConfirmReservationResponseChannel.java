package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ConfirmReservationResponseChannel extends ServerChannelRegistry {
    private static final String TOPIC = "/controller/+/confirm_reservation/#";
    private static final String KEY_SLOT_NUMBER = "slot_no";

    public ConfirmReservationResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseOjbect(int slotNumber) {
        JsonObject responseObject = new JsonObject();
        responseObject.add(KEY_SLOT_NUMBER, slotNumber);
        return responseObject;
    }
}
