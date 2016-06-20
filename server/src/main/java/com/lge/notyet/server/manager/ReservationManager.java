package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private ReservationManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
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

    public void makeReservation(int userId, int slotId, int reservationTimestamp, int confirmationNumber, double fee, int feeUnit, int gracePeriod, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("makeReservation: userId=" + userId + ", slotId=" + slotId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.insertReservation(sqlConnection, userId, slotId, reservationTimestamp, confirmationNumber, fee, feeUnit, gracePeriod, ar2 -> {
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

    public void startTransaction(int reservationId, Handler<AsyncResult<Void>> handler) {
        logger.info("startTransaction: reservationId=" + reservationId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                final int beginTs = (int) System.currentTimeMillis() / 1000;
                databaseProxy.insertTransaction(sqlConnection, reservationId, beginTs, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                    } else {
                        handler.handle(Future.succeededFuture());
                    }
                });
            }
        });
    }

    public void stopTransaction(int reservationId, Handler<AsyncResult<Void>> handler) {
        logger.info("stopTransaction: reservationId=" + reservationId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
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
}