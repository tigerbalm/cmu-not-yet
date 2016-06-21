package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.util.List;

public class GetFacilitiesResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facilities/get/#";
    public final static String KEY_RESULT= "facilities";

    public GetFacilitiesResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<JsonObject> facilityObjectList) {
        JsonObject responseObject = new JsonObject();
        JsonArray facilityObjectArray = new JsonArray();
        facilityObjectList.forEach(facilityObjectArray::add);
        responseObject.add(KEY_RESULT, facilityObjectArray);
        return responseObject;
    }
}