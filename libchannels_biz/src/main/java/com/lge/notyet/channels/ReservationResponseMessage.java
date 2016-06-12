package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

public class ReservationResponseMessage extends MqttNetworkMessage {

    public ReservationResponseMessage() {
        super(new JsonObject());
    }
}
