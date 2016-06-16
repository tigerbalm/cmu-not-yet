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
    public final static String KEY_RESULT = "result";

    public GetDBQueryResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<JsonArray> resultList) {
        JsonObject responseObject = new JsonObject();
        JsonArray resultArray = new JsonArray();
        resultList.forEach(resultArray::add);
        responseObject.add(KEY_RESULT, resultArray);
        return responseObject;
    }
}
