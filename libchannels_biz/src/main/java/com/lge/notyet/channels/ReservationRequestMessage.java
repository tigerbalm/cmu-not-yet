package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

abstract public class ReservationRequestMessage extends MqttNetworkMessage {

    public ReservationRequestMessage() {
        super(new JsonObject());
    }

    
}
