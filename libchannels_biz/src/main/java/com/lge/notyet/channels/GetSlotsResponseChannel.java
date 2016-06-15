package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.util.List;

public class GetSlotsResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facility/+/slots/get/#";

    public GetSlotsResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<JsonObject> slotObjectList) {
        JsonObject responseObject = new JsonObject();
        JsonArray slotObjectArray = new JsonArray();
        for (JsonObject slotObject : slotObjectList) {
            slotObjectArray.add(slotObject);
        }
        responseObject.add("slots", slotObjectArray);
        return responseObject;
    }
}
