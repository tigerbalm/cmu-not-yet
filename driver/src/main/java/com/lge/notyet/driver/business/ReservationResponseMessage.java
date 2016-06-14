package com.lge.notyet.driver.business;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;


public class ReservationResponseMessage extends MqttNetworkMessage {

    private static final String RESULT = "success";
    private static final String CONFIRMATION_NUMBER = "confirmation_no";
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

    public ReservationResponseMessage setFailCause(String cause) {
        mMessage.add(FAIL_CAUSE, cause);
        return this;
    }

    public int getResult() {
        return mMessage.get(RESULT).asInt();
    }

    public long getConfirmationNumber() {
        return mMessage.get(CONFIRMATION_NUMBER).asLong();
    }

    public String getFailCause() {
        return mMessage.get(FAIL_CAUSE).asString();
    }

    public boolean validate() {
        return (mMessage.get(RESULT) != null) &&
                (mMessage.get(CONFIRMATION_NUMBER) != null);
    }
}
