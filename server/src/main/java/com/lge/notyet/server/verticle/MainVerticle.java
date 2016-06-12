package com.lge.notyet.server.verticle;

import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {
    private DatabaseProxy databaseProxy;
    private CommunicationProxy communicationProxy;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        communicationProxy = CommunicationProxy.getInstance(vertx);
        Future<Void> communicationReady = communicationProxy.start((topic, message) -> {
            System.out.println("message received");
            System.out.println(topic);
            System.out.println(message);
        });
        databaseProxy = DatabaseProxy.getInstance(vertx);
        Future<Void> databaseReady = databaseProxy.start();

        CompositeFuture.all(communicationReady, databaseReady).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        CompositeFuture.all(communicationProxy.stop(), databaseProxy.stop()).setHandler(ar -> {
            if (ar.succeeded()) {
                stopFuture.complete();
            } else {
                stopFuture.fail(ar.cause());
            }
        });
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getCanonicalName());
    }
}