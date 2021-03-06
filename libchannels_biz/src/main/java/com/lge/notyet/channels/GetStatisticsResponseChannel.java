package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class GetStatisticsResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/facility/statistics/get/#";
    public final static String KEY_COLUMNNAMES = "columnnames";
    public final static String KEY_VALUES = "values";

    public GetStatisticsResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(List<String> columnnameList, List<JsonArray> valuesList) {
        JsonObject responseObject = new JsonObject();
        JsonArray columnnamesArray = new JsonArray();
        columnnameList.forEach(columnnamesArray::add);
        responseObject.add(KEY_COLUMNNAMES, columnnamesArray);
        JsonArray valuesArray = new JsonArray();
        valuesList.forEach(valuesArray::add);
        responseObject.add(KEY_VALUES, valuesArray);
        return responseObject;
    }
}
