package com.lge.notyet.server.verticle;

import com.lge.notyet.server.manager.CommunicationManager;
import com.lge.notyet.server.manager.DatabaseManager;
import io.vertx.core.*;

public class MainVerticle extends AbstractVerticle {
    private DatabaseManager databaseManager;
    private CommunicationManager communicationManager;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        communicationManager = CommunicationManager.getInstance(vertx);
        Future<Void> communicationReady = communicationManager.start((topic, message) -> {
            System.out.println("message received");
            System.out.println(topic);
            System.out.println(message);
        });
        databaseManager = DatabaseManager.getInstance(vertx);
        Future<Void> databaseReady = databaseManager.start();

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
        CompositeFuture.all(communicationManager.stop(), databaseManager.stop()).setHandler(ar -> {
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