package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class GetStatisticsRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facility/statistics/get";
    private final static String KEY_SESSION_KEY = "session_key";
    private final static String KEY_DB_QUERY_KEY = "dbquery_key";

    public GetStatisticsRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC).getValue());
    }

    public static String getSessionKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_SESSION_KEY).asString();
    }

    public static String getKeyDbqueryKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_DB_QUERY_KEY).asString();
    }

    public static MqttNetworkMessage createRequestMessage(String sessionKey, String dbQueryKey) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_SESSION_KEY, sessionKey);
        requestObject.add(KEY_DB_QUERY_KEY, dbQueryKey);
        return new MqttNetworkMessage(requestObject);
    }
}
