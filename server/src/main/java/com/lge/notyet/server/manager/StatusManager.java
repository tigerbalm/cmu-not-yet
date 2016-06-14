package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.ReservableFacilitiesRequestChannel;
import com.lge.notyet.channels.ReservableFacilitiesResponseChannel;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

public class StatusManager {
    private static StatusManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;
    private final AuthenticationManager authenticationManager;

    private StatusManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);
        authenticationManager = AuthenticationManager.getInstance();

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new ReservableFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservableFacilities(message)).listen();
        // new ReservableFacilitiesRequestChannel(networkConnection).request(ReservableFacilitiesRequestChannel.createRequestMessage("ssssss"));
    }

    public static StatusManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new StatusManager();
            }
            return instance;
        }
    }

    public void getReservableFacilities(Handler<AsyncResult<List<JsonObject>>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservableFacilities(sqlConnection, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {});
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        handler.handle(Future.succeededFuture(userObjects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {});
                    }
                });
            }
        });
    }

    public void getReservableSlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservableSlots(sqlConnection, facilityId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {});
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        handler.handle(Future.succeededFuture(userObjects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {});
                    }
                });
            }
        });
    }

    private void getReservableFacilities(NetworkMessage message) {
        final String sessionKey = ReservationRequestChannel.getSessionKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_DRIVER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final boolean isDriver = ar1.result();
                if (!isDriver) {
                    communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                } else {
                    getReservableFacilities(ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            List<JsonObject> facilityObjects = ar2.result();
                            communicationProxy.responseSuccess(message, ReservableFacilitiesResponseChannel.createResponseObject(facilityObjects));
                        }
                    });
                }
            }
        });
    }
}
