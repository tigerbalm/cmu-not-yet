package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
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

public class FacilityManager {
    private static FacilityManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;
    private final AuthenticationManager authenticationManager;

    private FacilityManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);
        authenticationManager = AuthenticationManager.getInstance();

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new ReservableFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservableFacilities(message)).listen();
        new UpdateSlotStatusSubscribeChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateSlotStatus(uri, message)).listen();
        new GetFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getFacilities(uri, message)).listen();
        new GetSlotsResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getSlots(uri, message)).listen();

        // new ReservableFacilitiesRequestChannel(networkConnection).request(ReservableFacilitiesRequestChannel.createRequestMessage("ssssss"));
        // new UpdateSlotStatusPublishChannel(networkConnection, "p1", 1).notify(new MqttNetworkMessage(new JsonObject().add("occupied", 1)));
        // new GetFacilitiesRequestChannel(networkConnection).request(GetFacilitiesRequestChannel.createRequestMessage("qqqqqq"));
        // new GetSlotsRequestChannel(networkConnection, 1).request(GetSlotsRequestChannel.createRequestMessage("session2"));
    }

    public static FacilityManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new FacilityManager();
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
                        databaseProxy.closeConnection(sqlConnection, ar -> {
                        });
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.size() != 1) {
                            handler.handle(Future.failedFuture("NO_SLOT_EXIST"));
                            databaseProxy.closeConnection(sqlConnection, ar -> {
                            });
                        } else {
                            handler.handle(Future.succeededFuture(objects.get(0)));
                            databaseProxy.closeConnection(sqlConnection, ar -> {
                            });
                        }
                    }
                });
            }
        });
    }

    private void getFacilitySlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectFacilitySlots(sqlConnection, facilityId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        List<JsonObject> objects = ar2.result();
                        handler.handle(Future.succeededFuture(objects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
                    }
                });
            }
        });
    }

    private void getFacilities(int userId, Handler<AsyncResult<List<JsonObject>>> handler) {
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectUserFacilities(sqlConnection, userId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        List<JsonObject> objects = ar2.result();
                        handler.handle(Future.succeededFuture(objects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
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
                final int occupiedTs = occupied ? ((int) System.currentTimeMillis() / 1000) : -1;
                databaseProxy.updateSlotOccupied(sqlConnection, slotId, occupied, occupiedTs, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, false, ar -> {
                        });
                    } else {
                        handler.handle(Future.succeededFuture());
                        databaseProxy.closeConnection(sqlConnection, true, ar -> {
                        });
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
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        handler.handle(Future.succeededFuture(userObjects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
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
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        handler.handle(Future.succeededFuture(userObjects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
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
                getReservableFacilities(ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        List<JsonObject> facilityObjects = ar2.result();
                        communicationProxy.responseSuccess(message, ReservableFacilitiesResponseChannel.createResponseObject(facilityObjects));
                    }
                });
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

    private void getFacilities(Uri uri, NetworkMessage message) {
        final String sessionKey = GetFacilitiesRequestChannel.getSessionKey(message);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final int userId = ar1.result().get("id").asInt();
                getFacilities(userId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        communicationProxy.responseSuccess(message, GetFacilitiesResponseChannel.createResponseObject(ar2.result()));
                    }
                });
            }
        });
    }

    private void getSlots(Uri uri, NetworkMessage message) {
        final String sessionKey = GetSlotsRequestChannel.getSessionKey(message);
        final int facilityId = GetSlotsRequestChannel.getFacilityId(uri);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userId = userObject.get("id").asInt();
                final int userType = userObject.get("type").asInt();

                if (userType != User.USER_TYPE_ATTENDANT) {
                    communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                } else {
                    getFacilities(userId, ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            final List<JsonObject> facilityObjectList = ar2.result();
                            boolean isAttendantFacility = false;
                            for (JsonObject facilityObject : facilityObjectList) {
                                if (facilityObject.get("id").asInt() == facilityId) {
                                    isAttendantFacility = true;
                                    break;
                                }
                            }
                            if (!isAttendantFacility) {
                                communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                            } else {
                                getFacilitySlots(facilityId, ar3 -> {
                                    if (ar3.failed()) {
                                        communicationProxy.responseFail(message, ar3.cause());
                                    } else {
                                        communicationProxy.responseSuccess(message, GetSlotsResponseChannel.createResponseObject(ar3.result()));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
