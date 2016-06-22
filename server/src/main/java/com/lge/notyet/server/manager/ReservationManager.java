package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.crypto.SureParkCrypto;
import com.lge.notyet.server.exception.InvalidConfirmationNumberException;
import com.lge.notyet.server.exception.NoReservationExistException;
import com.lge.notyet.server.proxy.CreditCardProxy;
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
import java.util.*;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Vertx vertx;
    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CreditCardProxy creditCardProxy;

    private final Map<Integer, Long> timerMap = new HashMap<>(); // reservationId to timerId
    private final Set<Listener> listenerSet = new ConcurrentHashSet<>();

    public interface Listener {
        void onReservationExpired(int reservationId);
        void onTransactionStarted(int reservationId);
        void onTransactionEnded(int reservationId);
    }

    public void registerListener(Listener listener) {
        listenerSet.add(listener);
    }

    private ReservationManager(Vertx vertx) {
        this.vertx = vertx;
        this.logger = LoggerFactory.getLogger(ReservationManager.class);
        this.databaseProxy = DatabaseProxy.getInstance(null);
        this.creditCardProxy = CreditCardProxy.getInstance();

        setCheckingExpiredReservationTimers();
    }

    public static ReservationManager getInstance(Vertx vertx) {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new ReservationManager(vertx);
            }
            return instance;
        }
    }

    private void setCheckingExpiredReservationTimer(int reservationId, int expirationTs) {
        logger.info("setCheckingExpiredReservationTimer: reservationId=" + reservationId + ", expirationTs=" + expirationTs);
        int currentTs = (int) Instant.now().getEpochSecond();
        int timerTs = expirationTs - currentTs;
        if (timerTs > 1) {
            logger.info("setCheckingExpiredReservationTimer: expirationTs=" + expirationTs + ", currentTs=" + currentTs + ", timerTs=" + timerTs);
            long timerId = vertx.setTimer((timerTs + 1) * 1000, id -> {
                checkExpiredReservations();
            });
            timerMap.put(reservationId, timerId);
        } else {
            checkExpiredReservations();
        }
    }

    private void setCheckingExpiredReservationTimers() {
        logger.info("setCheckingExpiredReservationTimers:");
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                ar1.cause().printStackTrace();
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectActivatedReservations(sqlConnection, ar2 -> {
                    databaseProxy.closeConnection(sqlConnection, ar -> {});
                    if (ar2.succeeded()) {
                        final List<JsonObject> reservationObjects = ar2.result();
                        logger.info("setCheckingExpiredReservationTimers: " + reservationObjects.size() + " activated reservations found");
                        for (JsonObject reservationObject : reservationObjects) {
                            int reservationId = reservationObject.get("id").asInt();
                            int expiredTs = reservationObject.get("expiration_ts").asInt();
                            final boolean hasTransaction = !reservationObject.get("begin_ts").isNull();
                            if (!hasTransaction) {
                                setCheckingExpiredReservationTimer(reservationId, expiredTs);
                            }
                        }
                    } else {
                        ar2.cause().printStackTrace();
                    }
                });
            }
        });
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
                        logger.info("checkExpiredReservations: expired reservations=" + reservationObjectList);
                        for (JsonObject reservationObject : reservationObjectList) {
                            final int reservationId = reservationObject.get("id").asInt();
                            cancelReservation(reservationId, ar -> {
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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new InvalidConfirmationNumberException())));
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
                databaseProxy.selectActivatedReservationByUserId(sqlConnection, userId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new NoReservationExistException())));
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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new NoReservationExistException())));
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
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new NoReservationExistException())));
                        } else {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(objects.get(0))));
                        }
                    }
                });
            }
        });
    }

    public void getActivatedReservation(int slotId, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getActivatedReservation: slotId=" + slotId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectActivatedReservation(sqlConnection, slotId, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar -> {});
                    } else {
                        List<JsonObject> objects = ar2.result();
                        if (objects.isEmpty()) {
                            logger.info("getActivatedReservation: slotId=" + slotId + " returns NoReservationExistException()");
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new NoReservationExistException())));
                        } else {
                            final JsonObject reservationObject = objects.get(0);
                            logger.info("getActivatedReservation: slotId=" + slotId + " returns " + reservationObject);
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(reservationObject)));
                        }
                    }
                });
            }
        });
    }

    public void makeReservation(int userId, int slotId, int reservationTs, double fee, int feeUnit, int gracePeriod, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("makeReservation: userId=" + userId + ", slotId=" + slotId);
        final int confirmationNumber = new Random().nextInt((9999 - 1000) + 1) + 1000;
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
                                        setCheckingExpiredReservationTimer(reservationId, expiredTs);
                                    }
                                }));
                            }
                        });
                    }
                });
            }
        });
    }

    public void reallocateSlot(int reservationId, int parkedSlotId, int reservedSlotId, Handler<AsyncResult<Void>> handler) {
        logger.info("reallocateSlot: reservationId=" + reservationId + ", parkedSlotId=" + parkedSlotId + ", reservedSlotId=" + reservedSlotId);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.updateReservationSlot(sqlConnection, reservationId, parkedSlotId, ar2 -> {
                   if (ar2.failed()) {
                       databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar2.cause())));
                   } else {
                       getActivatedReservation(parkedSlotId, ar3 -> {
                           if (ar3.failed()) {
                                if (ar3.cause() instanceof NoReservationExistException) { // parked slot is not reserved one
                                    logger.info("reallocateSlot: parked slot is not reserved one");
                                    databaseProxy.updateSlotReserved(sqlConnection, parkedSlotId, true, ar4 -> {
                                        if (ar4.failed()) {
                                            databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar4.cause())));
                                        } else {
                                            databaseProxy.updateSlotReserved(sqlConnection, reservedSlotId, false, ar5 -> {
                                                if (ar5.failed()) {
                                                    databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar5.cause())));
                                                } else {
                                                    databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    logger.info("reallocateSlot: failed to get reservation from parkedSlotId=" + parkedSlotId);
                                    databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar3.cause())));
                                }
                           } else { // parked slot is reserved one
                                logger.info("reallocateSlot: parked slot is reserved one, swap slot between two reservations");
                                final JsonObject reservationObject2 = ar3.result();
                                final int reservationId2 = reservationObject2.get("id").asInt();
                                databaseProxy.updateReservationSlot(sqlConnection, reservationId2, reservedSlotId, ar6 -> {
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
                        final int userId = reservationObject.get("user_id").asInt();
                        final int slotId = reservationObject.get("slot_id").asInt();
                        final int reservationId = reservationObject.get("id").asInt();
                        final double fee = reservationObject.get("fee").asDouble();
                        final int feeUnit = reservationObject.get("fee_unit").asInt();
                        final int beginTs = reservationObject.get("begin_ts").asInt();
                        final double revenue = Math.ceil(((double)endTs - (double)beginTs) / (double)feeUnit) * fee;

                        databaseProxy.selectUser(sqlConnection, userId, ar3 -> {
                            if (ar3.failed()) {
                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar3.cause())));
                            } else {
                                final JsonObject userObject = ar3.result().get(0);
                                final String cardNumber = userObject.get("card_number").asString();
                                final String cardExpiration = userObject.get("card_expiration").asString();
                                creditCardProxy.makePayment(SureParkCrypto.decrypt(cardNumber), SureParkCrypto.decrypt(cardExpiration), revenue, ar4 -> {
                                    if (ar4.failed()) {
                                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar4.cause())));
                                    } else {
                                        final long paymentId = ar4.result();
                                        databaseProxy.updateTransaction(sqlConnection, reservationId, endTs, revenue, paymentId, ar5 -> {
                                            if (ar5.failed()) {
                                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar5.cause())));
                                            } else {
                                                databaseProxy.updateReservationActivated(sqlConnection, reservationId, false, ar6 -> {
                                                    if (ar6.failed()) {
                                                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar6.cause())));
                                                    } else {
                                                        databaseProxy.updateSlotParked(sqlConnection, slotId, false, ar7 -> {
                                                            if (ar7.failed()) {
                                                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar7.cause())));
                                                            } else {
                                                                databaseProxy.updateSlotReserved(sqlConnection, slotId, false, ar8 -> {
                                                                    if (ar8.failed()) {
                                                                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar8.cause())));
                                                                    } else {
                                                                        listenerSet.forEach(listener -> listener.onTransactionEnded(reservationId));
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
                        listenerSet.forEach(listener -> listener.onTransactionStarted(reservationId));
                        databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
                    }
                });
            }
        });
    }

    public void cancelReservation(int reservationId, Handler<AsyncResult<Void>> handler) {
        logger.info("cancelReservation: reservationId=" + reservationId);
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
                        databaseProxy.updateReservationActivated(sqlConnection, reservationId, false, ar3 -> {
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