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

public class FacilityManager {
    private static FacilityManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private FacilityManager() {
        logger = LoggerFactory.getLogger(FacilityManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
    }

    public static FacilityManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new FacilityManager();
            }
            return instance;
        }
    }

    public void getSlot(String controllerPhysicalId, int slotNumber, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("getSlot: controllerPhysicalId=" + controllerPhysicalId + ", slotNumber=" + slotNumber);
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

    public void getFacilitySlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> handler) {
        logger.info("getFacilitySlots: facilityId=" + facilityId);
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

    public void getFacilities(int userId, Handler<AsyncResult<List<JsonObject>>> handler) {
        logger.info("getFacilities: userId=" + userId);
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

    public void updateSlotOccupied(int slotId, boolean occupied, Handler<AsyncResult<Void>> handler) {
        logger.info("updateSlotOccupied: slotId=" + slotId + ", occupied=" + occupied);
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

    public void getReservableFacilities(Handler<AsyncResult<List<JsonObject>>> handler) {
        logger.info("getReservableFacilities:");
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
        logger.info("getReservableSlots: facilityId=" + facilityId);
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
}
