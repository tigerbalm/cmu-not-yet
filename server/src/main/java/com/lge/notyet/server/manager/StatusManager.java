package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
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
        new UpdateSlotStatusSubscribeChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateSlotStatus(uri, message)).listen();

        // new ReservableFacilitiesRequestChannel(networkConnection).request(ReservableFacilitiesRequestChannel.createRequestMessage("ssssss"));
        // new UpdateSlotStatusPublishChannel(networkConnection, "p1", 1).notify(new MqttNetworkMessage(new JsonObject().add("occupied", 1)));
    }

    public static StatusManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new StatusManager();
            }
            return instance;
        }
    }

    private void getSlot(String controllerPhysicalId, int slotNumber, Handler<AsyncResult<JsonObject>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectSlot(sqlConnection, controllerPhysicalId, slotNumber, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.size() != 1) {
                            handler.handle(Future.failedFuture("NO_SLOT_EXIST"));
                            databaseProxy.closeConnection(sqlConnection, ar -> {});
                        } else {
                            handler.handle(Future.succeededFuture(objects.get(0)));
                            databaseProxy.closeConnection(sqlConnection, ar -> {});
                        }
                    }
                });
            }
        });
    }

    private void updateSlotOccupied(int slotId, boolean occupied, Handler<AsyncResult<Void>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.updateSlotOccupied(sqlConnection, slotId, occupied, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                    } else {
                        handler.handle(Future.succeededFuture());
                        databaseProxy.closeConnection(sqlConnection, true, ar -> {});
                    }
                });
            }
        });
    }

    private void getReservableFacilities(Handler<AsyncResult<List<JsonObject>>> handler) {
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

    private void getReservableSlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> handler) {
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

    private void updateSlotStatus(Uri uri, NetworkMessage message) {
        final String controllerPhysicalId = UpdateSlotStatusPublishChannel.getControllerPhysicalId(uri);
        final int slotNumber = UpdateSlotStatusPublishChannel.getSlotNumber(uri);
        final boolean occupied = UpdateSlotStatusPublishChannel.isOccupied(message);

        getSlot(controllerPhysicalId, slotNumber, ar1 -> {
            if (ar1.failed()) {
                ar1.cause().printStackTrace();
            } else {
                final JsonObject slotObject = ar1.result();
                final int slotId = slotObject.get("id").asInt();
                updateSlotOccupied(slotId, occupied, ar2 -> {
                   if (ar2.failed()) {
                       ar2.cause().printStackTrace();
                   } else {
                       logger.info("updateSlotStatus: slot=" + slotObject + " updated occupied=" + occupied);
                   }
                });
            }
        });
    }
}
