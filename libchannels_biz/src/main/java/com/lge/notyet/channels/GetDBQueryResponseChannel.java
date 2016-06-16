package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.util.List;

public class GetDBQueryResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facility/dbquery/get/#";

    public GetDBQueryResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<JsonObject> metaAndResultSetObjectList) {
        JsonObject responseObject = new JsonObject();
        JsonArray metaAndResultSetObjectArray = new JsonArray();
        for (JsonObject metaAndResultSetObject : metaAndResultSetObjectList) {
            metaAndResultSetObjectArray.add(metaAndResultSetObject);
        }
        responseObject.add("metaAndResultSet", metaAndResultSetObjectArray);
        return responseObject;
    }
}
