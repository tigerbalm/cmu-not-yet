package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.exception.SureParkException;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Vertx vertx;
    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private final Map<Integer, Long> timerMap = new HashMap<>(); // reservationId to timerId
    private final Set<Listener> listenerSet = new ConcurrentHashSet<>();

    public interface Listener {
        void onReservationExpired(int reservationId);
    }

    public void registerListener(Listener listener) {
        listenerSet.add(listener);
    }

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
        logger.info("checkExpiredReservations:");
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                ar1.cause().printStackTrace();
            } else {
                final SQLConnection sqlConnection = ar1.result();
                final int currentTs = (int) Instant.now().getEpochSecond();
                databaseProxy.selectExpiredReservation(sqlConnection, currentTs, ar2 -> {
                    databaseProxy.closeConnection(sqlConnection, ar -> {});
                    if (ar2.failed()) {
                        ar2.cause().printStackTrace();
                    } else {
                        final List<JsonObject> reservationObjectList = ar2.result();
                        for (JsonObject reservationObject : reservationObjectList) {
                            final int reservationId = reservationObject.get("id").asInt();
                            removeReservation(reservationId, ar -> {
                                if (ar.succeeded()) {
                                    listenerSet.forEach(listener -> listener.onReservationExpired(reservationId));
                                }
                            });
                        }
                    }
                });
            }
        });
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
                final int expiredTs = reservationTs + gracePeriod;
                databaseProxy.insertReservation(sqlConnection, userId, slotId, reservationTs, confirmationNumber, fee, feeUnit, expiredTs, ar2 -> {
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
                                        JsonObject reservationObject = ar4.result();
                                        handler.handle(Future.succeededFuture(reservationObject));

                                        int reservationId = reservationObject.get("id").asInt();
                                        int currentTs = (int) Instant.now().getEpochSecond();
                                        long timerTs = expiredTs - currentTs;
                                        logger.info("makeReservation: expiredTs=" + expiredTs + ", currentTs=" + currentTs + ", timerTs=" + timerTs);
                                        if (timerTs > 1) {
                                            long timerId = vertx.setTimer(timerTs * 1000, id -> {
                                                checkExpiredReservations();
                                            });
                                            timerMap.put(reservationId, timerId);
                                        }
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
        final int endTs = (int) Instant.now().getEpochSecond();
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
                final int beginTs = (int) Instant.now().getEpochSecond();
                databaseProxy.insertTransaction(sqlConnection, reservationId, beginTs, ar2 -> {
                    if (ar2.failed()) {
                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar2.cause())));
                    } else {
                        vertx.cancelTimer(timerMap.get(reservationId));
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