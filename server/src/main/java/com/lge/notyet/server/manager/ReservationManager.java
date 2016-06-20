package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.exception.SureParkException;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Vertx vertx;
    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private ReservationManager(Vertx vertx) {
        this.vertx = vertx;
        this.logger = LoggerFactory.getLogger(ReservationManager.class);
        this.databaseProxy = DatabaseProxy.getInstance(null);
    }

    public static ReservationManager getInstance(Vertx vertx) {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new ReservationManager(vertx);
            }
            return instance;
        }
    }

    private void checkExpiredReservations() {

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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(SureParkException.createInvalidConfirmationNumberException())));
                        } else {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(objects.get(0))));
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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(SureParkException.createNoReservationExistException())));
                        } else {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(objects.get(0))));
                        }
                    }
                });
            }
        });
    }

    public void getReservation(String controllerPhysicalId, int slotNumber, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getReservation: controllerPhysicalId=" + controllerPhysicalId + ", slotNumber=" + slotNumber);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectReservation(sqlConnection, controllerPhysicalId, slotNumber, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(SureParkException.createNoReservationExistException())));
                        } else {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(objects.get(0))));
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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(SureParkException.createNoReservationExistException())));
                        } else {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(objects.get(0))));
                        }
                    }
                });
            }
        });
    }

    public void makeReservation(int userId, int slotId, int reservationTs, int confirmationNumber, double fee, int feeUnit, int gracePeriod, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("makeReservation: userId=" + userId + ", slotId=" + slotId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.insertReservation(sqlConnection, userId, slotId, reservationTs, confirmationNumber, fee, feeUnit, reservationTs + gracePeriod, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                    } else {
                        databaseProxy.updateSlotReserved(sqlConnection, slotId, true, ar3 -> {
                            if (ar3.failed()) {
                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar3.cause())));
                            } else {
                                databaseProxy.closeConnection(sqlConnection, true, ar -> getReservationByConfirmationNumber(confirmationNumber, ar4 -> {
                                    if (ar4.failed()) {
                                        handler.handle(Future.failedFuture(ar4.cause()));
                                    } else {
                                        handler.handle(Future.succeededFuture(ar4.result()));
                                    }
                                }));
                            }
                        });
                    }
                });
            }
        });
    }

    public void finalizeReservation(String controllerPhysicalId, int slotNumber, Handler<AsyncResult<Void>> handler) {
        final int endTs = (int) System.currentTimeMillis() / 1000;
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                getReservation(controllerPhysicalId, slotNumber, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                    } else {
                        final JsonObject reservationObject = ar2.result();
                        final int slotId = reservationObject.get("slot_id").asInt();
                        final int reservationId = reservationObject.get("id").asInt();
                        final double fee = reservationObject.get("fee").asDouble();
                        final int feeUnit = reservationObject.get("fee_unit").asInt();
                        final int beginTs = reservationObject.get("begin_ts").asInt();
                        final double revenue = (endTs - beginTs) / feeUnit * fee; // TODO: need to be more accurate

                        databaseProxy.updateTransaction(sqlConnection, reservationId, endTs, revenue, ar3 -> {
                            if (ar3.failed()) {
                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar3.cause())));
                            } else {
                                databaseProxy.updateReservationActivated(sqlConnection, reservationId, false, ar4 -> {
                                   if (ar4.failed()) {
                                       databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar4.cause())));
                                   } else {
                                       databaseProxy.updateSlotParked(sqlConnection, slotId, false, ar5 -> {
                                           if (ar5.failed()) {
                                               databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar5.cause())));
                                           } else {
                                               databaseProxy.updateSlotReserved(sqlConnection, slotId, false, ar6 -> {
                                                   if (ar6.failed()) {
                                                       databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar6.cause())));
                                                   } else {
                                                       databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
                                                   }
                                               });
                                           }
                                       });
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
                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar2.cause())));
                    } else {
                        databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
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
                                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar4.cause())));
                                    } else {
                                        databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
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