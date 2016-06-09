package com.lge.notyet.verticle;

import com.lge.notyet.lib.comm.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;

import java.net.InetAddress;

public class MainVerticle extends AbstractVerticle {
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
        final Future<Void> databaseReady = Future.future();
        JsonObject mysqlConfig = new JsonObject().
                put("host", "127.0.0.1").
                put("username", "dba").
                put("password", "dba").
                put("database", "sure-park");
        AsyncSQLClient mysqlClient = MySQLClient.createShared(vertx, mysqlConfig, "MySQLPool1");
        mysqlClient.getConnection(res -> {
            if (res.succeeded()) {
                System.out.println("database ready");
                databaseReady.complete();
            } else {
                databaseReady.fail(res.cause());
            }
        });
        return databaseReady;
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