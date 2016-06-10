package com.lge.notyet.server.verticle;

import com.lge.notyet.lib.comm.*;
import com.lge.notyet.server.manager.DatabaseManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Launcher;

import java.net.InetAddress;

public class MainVerticle extends AbstractVerticle {
    private DatabaseManager databaseManager;
    private INetworkChannel networkChannel;

    private Future<Void> prepareNetwork() {
        final Future<Void> networkReady = Future.future();
        networkChannel = new MqttNetworkChannel((topic, msg) -> {
            // TODO
        });
        networkChannel.connect(InetAddress.getLoopbackAddress(), new INetworkCallback() {
            @Override
            public void onConnected() {
                System.out.println("network ready");
                networkReady.complete();
            }

            @Override
            public void onConnectFailed() {
                System.out.println("failed to connect");
                networkReady.fail("failed to connect");
            }

            @Override
            public void onLost() {
                // TODO
            }
        });
        return networkReady;
    }

    private Future<Void> prepareDatabase() {
        databaseManager = DatabaseManager.getInstance(vertx);
        return databaseManager.start();
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        CompositeFuture.all(prepareNetwork(), prepareDatabase()).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getCanonicalName());
    }
}