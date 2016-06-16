package com.lge.notyet.driver.business;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;


public class ReservationResponseMessage extends MqttNetworkMessage {

    private static final String RESULT = "success";
    private static final String CONFIRMATION_NUMBER = "confirmation_no";
    private static final String RESERVATION_ID = "id";
    private static final String FAIL_CAUSE = "cause";

    public ReservationResponseMessage() {
        super(new JsonObject());
    }

    // TypeCasting :(
    public ReservationResponseMessage(MqttNetworkMessage message) {
        this();
        mMessage = message.getMessage();
    }

    public ReservationResponseMessage setResult(int result) {
        mMessage.add(RESULT, result);
        return this;
    }

    public ReservationResponseMessage setConfirmationNumber(long confirmationNumber) {
        mMessage.add(CONFIRMATION_NUMBER, confirmationNumber);
        return this;
    }

    public ReservationResponseMessage setReservationId(long reservationId) {
        mMessage.add(RESERVATION_ID, reservationId);
        return this;
    }

    public int getResult() {
        return mMessage.get(RESULT).asInt();
    }

    public int getConfirmationNumber() {
        return mMessage.get(CONFIRMATION_NUMBER).asInt();
    }

    public int getReservationId() {
        return mMessage.get(RESERVATION_ID).asInt();
    }

    public String getFailCause() {
        return mMessage.get(FAIL_CAUSE).asString();
    }

    public boolean validate() {
        return (mMessage.get(RESULT) != null) &&
                (mMessage.get(CONFIRMATION_NUMBER) != null);
    }
}
