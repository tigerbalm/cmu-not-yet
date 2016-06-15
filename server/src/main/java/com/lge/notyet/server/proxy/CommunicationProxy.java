package com.lge.notyet.server.proxy;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommunicationProxy {
    private static CommunicationProxy instance = null;

    private static final String HOST = "192.168.1.21";
    // private static final String HOST = "localhost";

    private Vertx vertx;
    private Logger logger;
    private INetworkConnection networkConnection;

    private CommunicationProxy(Vertx vertx) {
        this.vertx = vertx;
        this.logger = LoggerFactory.getLogger(CommunicationProxy.class);
    }

    public static CommunicationProxy getInstance(Vertx vertx) {
        synchronized (CommunicationProxy.class) {
            if (instance == null) {
                instance = new CommunicationProxy(vertx);
            }
            return instance;
        }
    }

    public Future<Void> start() {
        final Future<Void> future = Future.future();
        networkConnection = new MqttNetworkConnection(null);
        // networkConnection = new MqttPassiveRedundancyNetworkConnection("server", new MqttNetworkConnection(null));
        try {
            logger.info("making MQTT connection (" + HOST + ")");
            networkConnection.connect(InetAddress.getByName(HOST), new INetworkCallback() {
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

    public void responseFail(NetworkMessage message, String cause) {
        logger.info("responseFail: cause=" + cause);
        JsonObject responseObject = new JsonObject();
        responseObject.add("success", 0);
        responseObject.add("cause", cause);
        message.responseFor(new MqttNetworkMessage(responseObject));
    }

    public void responseFail(NetworkMessage message, Throwable cause) {
        responseFail(message, cause.getMessage());
    }
}