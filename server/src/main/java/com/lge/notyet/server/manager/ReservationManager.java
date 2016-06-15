package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.*;
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
import java.util.Random;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;
    private final AuthenticationManager authenticationManager;
    private final FacilityManager facilityManager;

    private ReservationManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);
        authenticationManager = AuthenticationManager.getInstance();
        facilityManager = FacilityManager.getInstance();

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();

        new ReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> makeReservation(uri, message)).listen();
        new ConfirmReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> confirmReservation(uri, message)).listen();
        new GetReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservation(uri, message)).listen();
        new CancelReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> cancelReservation(uri, message)).listen();

        // new ReservationRequestChannel(networkConnection, 1).request(ReservationRequestChannel.createRequestMessage("ssssss", System.currentTimeMillis()));
        // new ConfirmReservationRequestChannel(networkConnection, "p1").request(ConfirmReservationRequestChannel.createRequestMessage(9999));
        // new GetReservationRequestChannel(networkConnection).request(GetReservationRequestChannel.createRequestMessage("ssssss"));
        // new CancelReservationRequestChannel(networkConnection, 1).request(CancelReservationRequestChannel.createRequestMessage("ssssss"));
    }

    public static ReservationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new ReservationManager();
            }
            return instance;
        }
    }

    public void getReservationByConfirmationNumber(int confirmationNumber, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getReservationByConfirmationNumber: confirmationNumber=" + confirmationNumber);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservationByConfirmationNumber(sqlConnection, confirmationNumber, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            handler.handle(Future.failedFuture("INVALID_CONFIRMATION_NO"));
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

    public void getReservationByUserId(int userId, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getReservationByUserId: userId=" + userId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservationByUserId(sqlConnection, userId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            handler.handle(Future.failedFuture("NO_RESERVATION_EXIST"));
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

    public void getReservation(int reservationId, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getReservation: reservationId=" + reservationId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservation(sqlConnection, reservationId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            handler.handle(Future.failedFuture("NO_RESERVATION_EXIST"));
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

    public void makeReservation(int userId, int slotId, int reservationTimestamp, int confirmationNumber, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("makeReservation: userId=" + userId + ", slotId=" + slotId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.insertReservation(sqlConnection, userId, slotId, reservationTimestamp, confirmationNumber, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                    } else {
                        databaseProxy.updateSlotReserved(sqlConnection, slotId, true, ar3 -> {
                            if (ar3.failed()) {
                                handler.handle(Future.failedFuture(ar3.cause()));
                                databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                            } else {
                                databaseProxy.closeConnection(sqlConnection, true, ar -> {});
                                getReservationByConfirmationNumber(confirmationNumber, ar4 -> {
                                    if (ar4.failed()) {
                                        handler.handle(Future.failedFuture(ar4.cause()));
                                    } else {
                                        handler.handle(Future.succeededFuture(ar4.result()));
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void removeReservation(int reservationId, Handler<AsyncResult<Void>> handler) {
        logger.info("removeReservation: reservationId=" + reservationId);
        getReservation(reservationId, ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final int slotId = ar1.result().get("slot_id").asInt();
                databaseProxy.openConnection(ar2 -> {
                    if (ar1.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                    } else {
                        final SQLConnection sqlConnection = ar2.result();
                        databaseProxy.deleteReservation(sqlConnection, reservationId, ar3 -> {
                            if (ar3.failed()) {
                                handler.handle(Future.failedFuture(ar3.cause()));
                                databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                            } else {
                                databaseProxy.updateSlotReserved(sqlConnection, slotId, false, ar4 -> {
                                    if (ar3.failed()) {
                                        handler.handle(Future.failedFuture(ar4.cause()));
                                        databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                                    } else {
                                        handler.handle(Future.succeededFuture());
                                        databaseProxy.closeConnection(sqlConnection, true, ar -> {});
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void makeReservation(Uri uri, NetworkMessage message) {
        final String sessionKey = ReservationRequestChannel.getSessionKey(message);
        final int facilityId = ReservationRequestChannel.getFacilityId(uri);
        final int reservationTimestamp = ReservationRequestChannel.getReservationTimestamp(message);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userId = userObject.get("id").asInt();
                facilityManager.getReservableSlots(facilityId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        final List<JsonObject> slotObjects = ar2.result();
                        if (slotObjects.isEmpty()) {
                            communicationProxy.responseFail(message, "NO_AVAILABLE_SLOT");
                        } else {
                            final int slotId = slotObjects.get(0).get("id").asInt();
                            final int confirmationNumber = new Random().nextInt((9999 - 1000) + 1) + 1000;
                            makeReservation(userId, slotId, reservationTimestamp, confirmationNumber, ar3 -> {
                                if (ar3.failed()) {
                                    communicationProxy.responseFail(message, ar3.cause());
                                } else {
                                    communicationProxy.responseSuccess(message, ar3.result());
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void confirmReservation(Uri uri, NetworkMessage message) {
        final int confirmationNumber = ConfirmReservationRequestChannel.getConfirmationNumber(message);

        logger.debug("confirmReservation: confirmationNumber=" + confirmationNumber);

        getReservationByConfirmationNumber(confirmationNumber, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject reservationObject = ar1.result();
                final int slotNumber = reservationObject.get("slot_no").asInt();
                communicationProxy.responseSuccess(message, ConfirmReservationResponseChannel.createResponseOjbect(slotNumber));
            }
        });
    }

    private void getReservation(Uri uri, NetworkMessage message) {
        final String sessionKey = GetReservationRequestChannel.getSessionKey(message);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userType = userObject.get("type").asInt();
                final int userId = userObject.get("id").asInt();
                if (userType != User.USER_TYPE_DRIVER) {
                    communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                } else {
                    getReservationByUserId(userId, ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            communicationProxy.responseSuccess(message, ar2.result());
                        }
                    });
                }
            }
        });
    }

    private void cancelReservation(Uri uri, NetworkMessage message) {
        final int reservationId = CancelReservationRequestChannel.getReservationId(uri);
        final String sessionKey = CancelReservationRequestChannel.getSessionKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_DRIVER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                removeReservation(reservationId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        communicationProxy.responseSuccess(message, new JsonObject());
                    }
                });
            }
        });
    }
}