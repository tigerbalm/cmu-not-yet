package com.lge.notyet.server.proxy;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.server.exception.InternalServerErrorException;
import com.lge.notyet.server.exception.SureParkException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommunicationProxy {
    private static CommunicationProxy instance = null;

    private Logger logger;
    private INetworkConnection networkConnection;

    private CommunicationProxy() {
        this.logger = LoggerFactory.getLogger(CommunicationProxy.class);
    }

    public static CommunicationProxy getInstance() {
        synchronized (CommunicationProxy.class) {
            if (instance == null) {
                instance = new CommunicationProxy();
            }
            return instance;
        }
    }

    public Future<Void> start(final String host, final boolean redundancy) {
        final Future<Void> future = Future.future();

        MqttNetworkConnection mqttNetworkConnection = new MqttNetworkConnection(null);
        if (redundancy) {
            networkConnection = new MqttPassiveRedundancyNetworkConnection("server", mqttNetworkConnection);
        } else {
            networkConnection = mqttNetworkConnection;
        }

        try {
            logger.info("making MQTT connection (" + host + ")");
            networkConnection.connect(InetAddress.getByName(host), new INetworkCallback() {
                @Override
                public void onConnected() {
                    logger.info("MQTT connection established");
                    future.complete();
                }

                @Override
                public void onConnectFailed() {
                    future.fail("failed to connect");
                }

                @Override
                public void onLost() {
                    logger.info("MQTT connection lost");
                }
            });
        } catch (UnknownHostException e) {
            future.fail(e);
        }
        return future;
    }

    public Future<Void> stop() {
        final Future<Void> future = Future.future();
        networkConnection.disconnect();
        future.complete();
        return future;
    }

    public INetworkConnection getNetworkConnection() {
        return networkConnection;
    }

    public void responseSuccess(NetworkMessage message, JsonObject responseObject) {
        logger.info("responseSuccess: responseObject=" + responseObject);
        responseObject.add("success", 1);
        message.responseFor(new MqttNetworkMessage(responseObject));
    }

    public void responseSuccess(NetworkMessage message) {
        responseSuccess(message, new JsonObject());
    }

    public void responseFail(NetworkMessage message, String cause) {
        logger.info("responseFail: cause=" + cause);
        JsonObject responseObject = new JsonObject();
        responseObject.add("success", 0);
        responseObject.add("cause", cause);
        message.responseFor(new MqttNetworkMessage(responseObject));
    }

    public void responseFail(NetworkMessage message, Throwable cause) {
        if (cause instanceof SureParkException) {
        } else {
            cause.printStackTrace();
            cause = new InternalServerErrorException();
        }
        responseFail(message, cause.getMessage());
    }
}