package com.lge.notyet.server.proxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

public class DatabaseProxy {
    private static DatabaseProxy instance = null;

    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "dba";
    private static final String PASSWORD = "dba";
    private static final String DATABASE = "sure-park";

    private Vertx vertx;
    private Logger logger;
    private AsyncSQLClient sqlClient;
    private SQLConnection sqlConnection;

    private DatabaseProxy(Vertx vertx) {
        this.vertx = vertx;
        this.logger = LoggerFactory.getLogger(DatabaseProxy.class);
    }

    public static DatabaseProxy getInstance(Vertx vertx) {
        synchronized (DatabaseProxy.class) {
            if (instance == null) {
                instance = new DatabaseProxy(vertx);
            }
            return instance;
        }
    }

    public Future<Void> start() {
        final Future<Void> future = Future.future();
        JsonObject mysqlConfig = new JsonObject().
                put("host", HOST).
                put("username", USERNAME).
                put("password", PASSWORD).
                put("database", DATABASE);
        sqlClient = MySQLClient.createShared(vertx, mysqlConfig);
        sqlClient.getConnection(ar -> {
            if (ar.succeeded()) {
                sqlConnection = ar.result();
                logger.info("database connected");
                sqlConnection.setAutoCommit(false, ar2 -> {
                    if (ar2.succeeded()) {
                        logger.info("set auto commit false");
                        future.complete();
                    } else {
                        future.fail(ar2.cause());
                    }
                });
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }

    public Future<Void> stop() {
        final Future<Void> future = Future.future();
        sqlConnection.close(res -> {
            future.complete();
        });
        return future;
    }

    public void commit(Handler<AsyncResult<Void>> resultHandler) {
        sqlConnection.commit(resultHandler);
    }

    public void rollback(Handler<AsyncResult<Void>> resultHandler) {
        sqlConnection.rollback(resultHandler);
    }

    private void singleItemReturnQuery(String sql, Handler<AsyncResult<JsonObject>> resultHandler) {
        sqlConnection.query(sql, ar -> {
            resultHandler.handle(new AsyncResult<JsonObject>() {
                @Override
                public JsonObject result() {
                    return ar.result().getRows().get(0);
                }

                @Override
                public Throwable cause() {
                    return ar.cause();
                }

                @Override
                public boolean succeeded() {
                    return ar.succeeded();
                }

                @Override
                public boolean failed() {
                    return ar.failed();
                }
            });
        });
    }

    private void multipleItemReturnQuery(String sql, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        sqlConnection.query(sql, ar -> {
            resultHandler.handle(new AsyncResult<List<JsonObject>>() {
                @Override
                public List<JsonObject> result() {
                    return ar.result().getRows();
                }

                @Override
                public Throwable cause() {
                    return ar.cause();
                }

                @Override
                public boolean succeeded() {
                    return ar.succeeded();
                }

                @Override
                public boolean failed() {
                    return ar.failed();
                }
            });
        });
    }

    private void updateSingleItem(String sql, JsonArray parameters, Handler<AsyncResult<UpdateResult>> resultHandler) {
        sqlConnection.updateWithParams(sql, parameters, resultHandler);
    }

    public void getUser(int userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user where id = " + userId, resultHandler);
    }

    public void getUser(String email, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user where email=\'" + email + "\' and password=\'" + password + "\'", resultHandler);
    }

    public void getUser(String sessionKey, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user, session where session_key=\'" + sessionKey + "\'", resultHandler);
    }

    public void getReservableFacilities(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select id, name " +
                "from facility " +
                "where id in ( " +
                "select controller.facility_id " +
                "from controller inner join slot on controller.id = slot.controller_id " +
                "where controller.available = 1 " +
                "and slot.occupied = 0 " +
                "and slot.reserved = 0)";
        multipleItemReturnQuery(sql, resultHandler);
    }

    public void getReservableSlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select slot.id as id, controller.facility_id, slot.controller_id" +
                " from controller inner join slot on controller.id = slot.controller_id" +
                " where controller.facility_id = " + facilityId +
                " and controller.available = 1" +
                " and slot.occupied = 0" +
                " and slot.reserved = 0";
        multipleItemReturnQuery(sql, resultHandler);
    }

    public void updateSlotOccupied(int slotId, boolean occupied, Handler<AsyncResult<UpdateResult>> resultHandler) {
        String sql = "update slot set occupied = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(occupied ? 1 : 0);
        parameters.add(slotId);
        updateSingleItem(sql, parameters, resultHandler);
    }

    public void updateSlotReserved(int slotId, boolean reserved, Handler<AsyncResult<UpdateResult>> resultHandler) {
        String sql = "update slot set reserved = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(reserved ? 1 : 0);
        parameters.add(slotId);
        updateSingleItem(sql, parameters, resultHandler);
    }

    public void updateControllerAvailable(int controllerId, boolean available, Handler<AsyncResult<UpdateResult>> resultHandler) {
        String sql = "update controller set available = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(available ? 1 : 0);
        parameters.add(controllerId);
        updateSingleItem(sql, parameters, resultHandler);
    }
}