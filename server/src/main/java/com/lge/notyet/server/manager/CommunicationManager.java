package com.lge.notyet.server.manager;

import com.lge.notyet.lib.comm.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommunicationManager {
    private static CommunicationManager instance = null;

    private static final String HOST = "127.0.0.1";

    private Vertx vertx;
    private INetworkChannel networkChannel;

    private CommunicationManager(Vertx vertx) {
        this.vertx = vertx;
    }

    public static CommunicationManager getInstance(Vertx vertx) {
        synchronized (CommunicationManager.class) {
            if (instance == null) {
                instance = new CommunicationManager(vertx);
            }
            return instance;
        }
    }

    public Future<Void> start(IMessageCallback messageCallback) {
        final Future<Void> future = Future.future();
        networkChannel = new MqttNetworkChannel(null, messageCallback);
        try {
            networkChannel.connect(InetAddress.getByName(HOST), new INetworkCallback() {
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
        networkChannel.disconnect();
        future.complete();
        return future;
    }
}
