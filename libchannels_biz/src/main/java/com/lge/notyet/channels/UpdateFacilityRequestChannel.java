package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class UpdateFacilityRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facility/%d/update";
    private final static String KEY_SESSION_KEY = "session_key";
    private final static String KEY_NAME = "name";
    private final static String KEY_FEE = "fee";
    private final static String KEY_FEE_UNIT = "fee_unit";
    private final static String KEY_GRACE_PERIOD = "grace_period";

    private int facilityId = 0;

    public UpdateFacilityRequestChannel(INetworkConnection networkConnection, int facilityId) {
        super(networkConnection);
        this.facilityId = facilityId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, facilityId).getValue());
    }

    public static int getFacilityId(Uri uri) {
        return Integer.parseInt((String) uri.getPathSegments().get(2));
    }

    public static String getSessionKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_SESSION_KEY).asString();
    }

    public static String getFacilityName(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_NAME).asString();
    }

    public static double getFee(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_FEE).asDouble();
    }

    public static int getFeeUnit(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_FEE_UNIT).asInt();
    }

    public static int getGracePeriod(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_GRACE_PERIOD).asInt();
    }

    public static MqttNetworkMessage createRequestMessage(String sessionKey, String name, String fee, String fee_unit, String grace_period) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_SESSION_KEY, sessionKey);
        requestObject.add(KEY_NAME, name);
        requestObject.add(KEY_FEE, Double.parseDouble(fee));
        requestObject.add(KEY_FEE_UNIT, Integer.parseInt(fee_unit));
        requestObject.add(KEY_GRACE_PERIOD, Integer.parseInt(grace_period));
        return new MqttNetworkMessage(requestObject);
    }
}