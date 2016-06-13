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

import java.util.List;

public class DatabaseProxy {
    private static DatabaseProxy instance = null;

    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "dba";
    private static final String PASSWORD = "dba";
    private static final String DATABASE = "sure-park";

    private Vertx vertx;
    private Logger logger;
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
        AsyncSQLClient sqlClient = MySQLClient.createShared(vertx, mysqlConfig);
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
        sqlConnection.close(res -> future.complete());
        return future;
    }

    public void commit(Handler<AsyncResult<Void>> resultHandler) {
        sqlConnection.commit(resultHandler);
    }

    public void rollback(Handler<AsyncResult<Void>> resultHandler) {
        sqlConnection.rollback(resultHandler);
    }

    private void singleItemReturnQuery(String sql, Handler<AsyncResult<JsonObject>> resultHandler) {
        sqlConnection.query(sql, ar -> resultHandler.handle(new AsyncResult<JsonObject>() {
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
                return ar.succeeded() && ar.result().getRows().size() == 1;
            }

            @Override
            public boolean failed() {
                return !succeeded();
            }
        }));
    }

    private void multipleItemReturnQuery(String sql, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        sqlConnection.query(sql, ar -> resultHandler.handle(new AsyncResult<List<JsonObject>>() {
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
        }));
    }

    private void updateWithParams(String sql, JsonArray parameters, Handler<AsyncResult<JsonArray>> resultHandler) {
        sqlConnection.updateWithParams(sql, parameters, ar -> resultHandler.handle(new AsyncResult<JsonArray>() {
            @Override
            public JsonArray result() {
                return ar.result().getKeys();
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
        }));
    }

    public void selectUser(int userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user where id = " + userId, resultHandler);
    }

    public void selectUser(String email, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user where email=\'" + email + "\' and password=\'" + password + "\'", resultHandler);
    }

    public void selectUser(String sessionKey, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from user, session where session_key=\'" + sessionKey + "\'", resultHandler);
    }

    public void selectSession(int userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        singleItemReturnQuery("select * from session where user_id=" + userId, resultHandler);
    }

    public void selectReservableFacilities(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
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

    public void selectReservableSlots(int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select slot.id as id, slot.number, controller.facility_id, slot.controller_id" +
                " from controller inner join slot on controller.id = slot.controller_id" +
                " where controller.facility_id = " + facilityId +
                " and controller.available = 1" +
                " and slot.occupied = 0" +
                " and slot.reserved = 0";
        multipleItemReturnQuery(sql, resultHandler);
    }

    public void selectReservation(int confirmationNumber, Handler<AsyncResult<JsonObject>> resultHandler) {
        String sql = "select reservation.id as id, reservation_ts, confirmation_number, user_id, user.email as user_email, slot_id, slot.number as slot_number, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name " +
                "from reservation inner join slot on reservation.slot_id = slot.id " +
                "inner join controller on controller.id = slot.controller_id " +
                "inner join facility on facility.id = controller.facility_id " +
                "inner join user on user.id = user_id " +
                "where confirmation_number = " + confirmationNumber;
        singleItemReturnQuery(sql, resultHandler);
    }

    public void selectReservations(int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select reservation.id as id, reservation_ts, confirmation_number, user_id, user.email as user_email, slot_id, slot.number as slot_number, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name " +
                "from reservation inner join slot on reservation.slot_id = slot.id " +
                "inner join controller on controller.id = slot.controller_id " +
                "inner join facility on facility.id = controller.facility_id " +
                "inner join user on user.id = user_id " +
                "where user_id = " + userId;
        multipleItemReturnQuery(sql, resultHandler);
    }

    public void updateSlotOccupied(int slotId, boolean occupied, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update slot set occupied = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(occupied ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(sql, parameters, resultHandler);
    }

    public void updateSlotReserved(int slotId, boolean reserved, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update slot set reserved = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(reserved ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(sql, parameters, resultHandler);
    }

    public void updateControllerAvailable(int controllerId, boolean available, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update controller set available = ? where id = ?";
        JsonArray parameters = new JsonArray();
        parameters.add(available ? 1 : 0);
        parameters.add(controllerId);
        updateWithParams(sql, parameters, resultHandler);
    }

    public void deleteReservation(int reservationId, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "delete from controller where id = ?";
        JsonArray parameters = new JsonArray().add(reservationId);
        updateWithParams(sql, parameters, resultHandler);
    }

    public void insertReservation(int userId, int slotId, int reservationTs, int confirmationNo, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into reservation(user_id, slot_id, reservation_ts, confirmation_number) values (?, ?, ?, ?)";
        JsonArray parameters = new JsonArray();
        parameters.add(userId);
        parameters.add(slotId);
        parameters.add(reservationTs);
        parameters.add(confirmationNo);
        updateWithParams(sql, parameters, resultHandler);
    }
}