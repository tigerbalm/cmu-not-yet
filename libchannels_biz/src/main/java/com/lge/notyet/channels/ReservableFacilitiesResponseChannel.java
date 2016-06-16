package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.util.List;

public class ReservableFacilitiesResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facility/reservable_list/#";
    private final static String KEY_FACILITIES = "facilities";

    public ReservableFacilitiesResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<JsonObject> reservableFacilityObjects) {
        JsonArray array = new JsonArray();
        reservableFacilityObjects.forEach(array::add);
        return new JsonObject().add(KEY_FACILITIES, array);
    }
}