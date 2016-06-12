package com.lge.notyet.server.verticle;

import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

public class MainVerticle extends AbstractVerticle {
    private DatabaseProxy databaseProxy;
    private CommunicationProxy communicationProxy;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        communicationProxy = CommunicationProxy.getInstance(vertx);
        Future<Void> communicationReady = communicationProxy.start((networkChannel, uri, message) -> {
            System.out.println("onRequested");
            System.out.println(uri);
            System.out.println(message);
        });
        databaseProxy = DatabaseProxy.getInstance(vertx);
        Future<Void> databaseReady = databaseProxy.start();

        CompositeFuture.all(communicationReady, databaseReady).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();

                Future<JsonObject> future1 = Future.future();
                Future<JsonObject> future2 = Future.future();
                Future<List<JsonObject>> future3 = Future.future();
                Future<List<JsonObject>> future4 = Future.future();
                Future<Void> failedFuture = Future.future();
                failedFuture.setHandler(far -> {
                    System.out.println("failed");
                });
                databaseProxy.getUser(0, future1.completer());
                future1.compose(userObject -> {
                    System.out.println(userObject);
                    databaseProxy.getUser("kkkkkk", future2.completer());
                }, failedFuture);
                future2.compose(userObject -> {
                    System.out.println(userObject);
                    databaseProxy.getReservableFacilities(future3.completer());
                }, failedFuture);
                future3.compose(facilityObjects -> {
                    System.out.println(facilityObjects);
                    databaseProxy.getReservableSlots(0, future4.completer());
                }, failedFuture);
                future4.compose(slotObjects -> {
                    System.out.println(slotObjects);
                }, null);
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