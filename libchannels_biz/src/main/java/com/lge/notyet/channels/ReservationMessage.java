package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

public class ReservationMessage extends MqttNetworkMessage {

    private static final String SESSION_KEY = "session_key";
    private static final String TIME_STAMP = "reservation_ts";

    public ReservationMessage() {
        super(new JsonObject());
    }

    public ReservationMessage setSessionKey(String sessionKey) {
        mMessage.add(SESSION_KEY, sessionKey);
        return this;
    }

    public ReservationMessage setTimeStamp(long epoch) {
        mMessage.add(TIME_STAMP, epoch);
        return this;
    }

    public String getSessionKey() {
        return mMessage.get(SESSION_KEY).asString();
    }

    public long getTimeStamp() {
        return mMessage.get(TIME_STAMP).asLong();
    }

    public boolean validate() {
        return (mMessage.get(SESSION_KEY) != null) &&
                (mMessage.get(TIME_STAMP) != null);
    }
}
