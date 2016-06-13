package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

public class ReservationMessage extends MqttNetworkMessage {

    private static final String SESSION_ID = "session_id";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String CREDIT_CARD_NUMBER = "credit_card_number";

    public ReservationMessage() {
        super(new JsonObject());
    }

    public ReservationMessage setSessionId(String sessionId) {
        mMessage.add(SESSION_ID, sessionId);
        return this;
    }

    public ReservationMessage setDate(String date) {
        mMessage.add(DATE, date);
        return this;
    }

    public ReservationMessage setTime(String time) {
        mMessage.add(TIME, time);
        return this;
    }

    public ReservationMessage setCreditCardNumber(String creditCardNumber) {
        mMessage.add(CREDIT_CARD_NUMBER, creditCardNumber);
        return this;
    }

    public String getSessionId() {
        return mMessage.get(SESSION_ID).asString();
    }

    public String getDate(String date) {
        return mMessage.get(DATE).asString();
    }

    public String getTime(String time) {
        return mMessage.get(TIME).asString();
    }

    public String getCreditCardNumber(String creditCardNumber) {
        return mMessage.get(CREDIT_CARD_NUMBER).asString();
    }

    public boolean validate() {
        return (mMessage.get(SESSION_ID) != null) &&
                (mMessage.get(DATE) != null) &&
                (mMessage.get(TIME) != null) &&
                (mMessage.get(CREDIT_CARD_NUMBER) != null);
    }
}