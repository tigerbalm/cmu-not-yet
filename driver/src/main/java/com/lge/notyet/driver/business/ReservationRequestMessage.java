package com.lge.notyet.driver.business;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

public class ReservationRequestMessage extends MqttNetworkMessage {

    private static final String SESSION_KEY = "session_key";
    private static final String TIME_STAMP = "reservation_ts";

    public ReservationRequestMessage() {
        super(new JsonObject());
    }

    public ReservationRequestMessage setSessionKey(String sessionKey) {
        mMessage.add(SESSION_KEY, sessionKey);
        return this;
    }

    public ReservationRequestMessage setTimeStamp(long epoch) {
        mMessage.add(TIME_STAMP, epoch);
        return this;
    }
}
