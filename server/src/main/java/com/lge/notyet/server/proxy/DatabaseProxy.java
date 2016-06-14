package com.lge.notyet.server.proxy;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.stream.Collectors;

public class DatabaseProxy {
    private static DatabaseProxy instance = null;

    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "dba";
    private static final String PASSWORD = "dba";
    private static final String DATABASE = "sure-park";

    private Vertx vertx;
    private Logger logger;
    private AsyncSQLClient sqlClient;

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
        io.vertx.core.json.JsonObject mysqlConfig = new io.vertx.core.json.JsonObject().
                put("host", HOST).
                put("username", USERNAME).
                put("password", PASSWORD).
                put("database", DATABASE);
        sqlClient = MySQLClient.createShared(vertx, mysqlConfig);
        future.complete();
        return future;
    }

    public Future<Void> stop() {
        final Future<Void> future = Future.future();
        sqlClient.close(ar -> future.complete());
        return future;
    }

    public void openConnection(Handler<AsyncResult<SQLConnection>> handler) {
        sqlClient.getConnection(ar -> {
            if (!ar.succeeded()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                final SQLConnection sqlConnection = ar.result();
                sqlConnection.setAutoCommit(false, ar2 -> {
                    if (!ar2.succeeded()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                    } else {
                        handler.handle(Future.succeededFuture(sqlConnection));
                    }
                });
            }
        });
    }

    public void closeConnection(SQLConnection connection, Handler<AsyncResult<Void>> handler) {
        closeConnection(connection, true, handler);
    }

    public void closeConnection(SQLConnection connection, boolean commit, Handler<AsyncResult<Void>> handler) {
        if (commit) {
            connection.commit(ar -> {
                connection.close(ar2 -> {
                    handler.handle(ar);
                });
            });
        } else {
            connection.rollback(ar -> {
                connection.close(ar2 -> {
                    handler.handle(ar);
                });
            });
        }
    }

    public void commit(SQLConnection connection, Handler<AsyncResult<Void>> resultHandler) {
        connection.commit(resultHandler);
    }

    private void query(SQLConnection connection, String sql, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        connection.query(sql, ar -> resultHandler.handle(new AsyncResult<List<JsonObject>>() {
            @Override
            public List<JsonObject> result() {
                return ar.result().getRows().stream().map(row -> JsonObject.readFrom(row.toString())).collect(Collectors.toList());
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

    private void updateWithParams(SQLConnection connection, String sql, io.vertx.core.json.JsonArray parameters, Handler<AsyncResult<JsonArray>> resultHandler) {
        connection.updateWithParams(sql, parameters, ar -> resultHandler.handle(new AsyncResult<JsonArray>() {
            @Override
            public JsonArray result() {
                JsonArray result = new JsonArray();
                io.vertx.core.json.JsonArray keys = ar.result().getKeys();
                for (int i = 0; i < keys.size(); i++) {
                    result.add(keys.getInteger(i)); // FIXME: assume that type of key is integer
                }
                return result;
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

    public void selectUser(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        query(connection, "select * from user where id = " + userId, resultHandler);
    }

    public void selectUser(SQLConnection connection, String email, String password, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        query(connection, "select * from user inner join session on user.id = session.user_id where email=\'" + email + "\' and password=\'" + password + "\'", resultHandler);
    }

    public void selectUser(SQLConnection connection, String sessionKey, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        query(connection, "select * from user inner join session on user.id = session.user_id where session_key=\'" + sessionKey + "\'", resultHandler);
    }

    public void selectSession(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        query(connection, "select * from session where user_id=" + userId, resultHandler);
    }

    public void selectReservableFacilities(SQLConnection connection, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select id, name " +
                "from facility " +
                "where id in ( " +
                "select controller.facility_id " +
                "from controller inner join slot on controller.id = slot.controller_id " +
                "where controller.available = 1 " +
                "and slot.occupied = 0 " +
                "and slot.reserved = 0)";
        query(connection, sql, resultHandler);
    }

    public void selectSlot(SQLConnection connection, String controllerPhysicalId, int slotNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select slot.id as id, number, occupied, reserved, controller_id\n" +
                "from controller inner join slot on controller.id = slot.controller_id\n" +
                "where physical_id = \'" + controllerPhysicalId + "\'" +
                "and number = " + slotNumber;
        query(connection, sql, resultHandler);
    }

    public void selectReservableSlots(SQLConnection connection, int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select slot.id as id, slot.number, controller.facility_id, slot.controller_id" +
                " from controller inner join slot on controller.id = slot.controller_id" +
                " where controller.facility_id = " + facilityId +
                " and controller.available = 1" +
                " and slot.occupied = 0" +
                " and slot.reserved = 0";
        query(connection, sql, resultHandler);
    }

    public void selectReservation(SQLConnection connection, int confirmationNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select reservation.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name " +
                "from reservation inner join slot on reservation.slot_id = slot.id " +
                "inner join controller on controller.id = slot.controller_id " +
                "inner join facility on facility.id = controller.facility_id " +
                "inner join user on user.id = user_id " +
                "where confirmation_no = " + confirmationNumber;
        query(connection, sql, resultHandler);
    }

    public void selectReservationByUserId(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "select reservation.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name " +
                "from reservation inner join slot on reservation.slot_id = slot.id " +
                "inner join controller on controller.id = slot.controller_id " +
                "inner join facility on facility.id = controller.facility_id " +
                "inner join user on user.id = user_id " +
                "where user_id = " + userId + " " +
                "order by reservation_ts";
        query(connection, sql, resultHandler);
    }

    public void updateSlotOccupied(SQLConnection connection, int slotId, boolean occupied, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update slot set occupied = ? where id = ?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(occupied ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateSlotReserved(SQLConnection connection, int slotId, boolean reserved, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update slot set reserved = ? where id = ?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reserved ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateControllerAvailable(SQLConnection connection, int controllerId, boolean available, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update controller set available = ? where id = ?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(available ? 1 : 0);
        parameters.add(controllerId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void deleteReservation(SQLConnection connection, int reservationId, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "delete from controller where id = ?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray().add(reservationId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertReservation(SQLConnection connection, int userId, int slotId, int reservationTs, int confirmationNo, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into reservation(user_id, slot_id, reservation_ts, confirmation_no) values (?, ?, ?, ?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        parameters.add(slotId);
        parameters.add(reservationTs);
        parameters.add(confirmationNo);
        updateWithParams(connection, sql, parameters, resultHandler);
    }
}