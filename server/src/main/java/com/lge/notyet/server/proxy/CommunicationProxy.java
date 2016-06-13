package com.lge.notyet.server.proxy;

import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommunicationProxy {
    private static CommunicationProxy instance = null;

    private static final String HOST = "localhost";

    private Vertx vertx;
    private INetworkConnection networkConnection;

    private CommunicationProxy(Vertx vertx) {
        this.vertx = vertx;
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
            networkConnection.connect(InetAddress.getByName(HOST), new INetworkCallback() {
                @Override
                public void onConnected() {
                    future.complete();
                }

                @Override
                public void onConnectFailed() {
                    future.fail("failed to connect");
                }

                @Override
                public void onLost() {
                    // TODO
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
}
