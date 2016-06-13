package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.channels.ReservationResponseChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ReservationManager {
    private static ReservationManager instance = null;

    private Logger logger;
    private DatabaseProxy databaseProxy;
    private CommunicationProxy communicationProxy;

    private ReservationManager() {
        this.logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new ReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> process(networkChannel, uri, message)).listen();
        new ReservationRequestChannel(networkConnection, 5).request(new MqttNetworkMessage(new JsonObject().add("aaa", "bbb")));
    }

    public static ReservationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new ReservationManager();
            }
            return instance;
        }
    }

    private void process(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        logger.debug("process: networkChannel=" + networkChannel + ", uri=" + uri + ", message=" + message);
        process(networkChannel, uri, message);
    }

    private void process(ReservationResponseChannel networkChannel, Uri uri, MqttNetworkMessage message) {
        logger.debug("process: networkChannel=" + networkChannel + ", uri=" + uri + ", message=" + message);
    }
}
