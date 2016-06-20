package com.lge.notyet.server.proxy;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.stream.Collectors;

public class DatabaseProxy {
    private static DatabaseProxy instance = null;

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

    public Future<Void> start(final String host, final String username, final String password) {
        final Future<Void> future = Future.future();
        io.vertx.core.json.JsonObject mysqlConfig = new io.vertx.core.json.JsonObject().
                put("host", host).
                put("username", username).
                put("password", password).
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

    public void rawQuery(SQLConnection connection, String sql, Handler<AsyncResult<List<JsonArray>>> resultHandler) {
        connection.query(sql, ar -> resultHandler.handle(new AsyncResult<List<JsonArray>>() {
            @Override
            public List<JsonArray> result() {
                return ar.result().getResults().stream().map(row -> JsonArray.readFrom(row.toString())).collect(Collectors.toList());
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

    private void queryWithParams(SQLConnection connection, String sql, io.vertx.core.json.JsonArray parameters, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        connection.queryWithParams(sql, parameters, ar -> resultHandler.handle(new AsyncResult<List<JsonObject>>() {
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

    public void selectUserByEmail(SQLConnection connection, String email, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(email);
        queryWithParams(connection, "select * from user where email=?", parameters, resultHandler);
    }

    public void selectUser(SQLConnection connection, String email, String password, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectUser: email=" + email + ", password=" + password);
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(email);
        parameters.add(password);
        queryWithParams(connection, "select * from user inner join session on user.id=session.user_id where email=? and password=?", parameters, resultHandler);
    }

    public void selectUser(SQLConnection connection, String sessionKey, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectUser: sessionKey=" + sessionKey);
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(sessionKey);
        queryWithParams(connection, "select * from user inner join session on user.id=session.user_id where session_key=?", parameters, resultHandler);
    }

    public void selectFacility(SQLConnection connection, int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectFacility: facilityId=" + facilityId);
        String sql = "select *" +
                " from facility" +
                " where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(facilityId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservableFacilities(SQLConnection connection, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservableFacilities:");
        String sql = "select id, name" +
                " from facility" +
                " where id in (" +
                " select controller.facility_id" +
                " from controller inner join slot on controller.id=slot.controller_id" +
                " where controller.available=1" +
                " and slot.parked=0" +
                " and slot.reserved=0)";
        query(connection, sql, resultHandler);
    }

    public void selectUserFacilities(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectUserFacilities: userId=" + userId);
        String sql = "select facility.id, facility.name, fee, fee_unit, grace_period" +
                " from facility inner join manage on facility.id=manage.facility_id inner join user on user.id=manage.facility_id" +
                " where user_id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectSlot(SQLConnection connection, String controllerPhysicalId, int slotNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectSlot: controllerPhysicalId=" + controllerPhysicalId + ", slotNumber=" + slotNumber);
        String sql = "select slot.id as id, number, parked, reserved, controller_id" +
                " from controller inner join slot on controller.id=slot.controller_id" +
                " where physical_id=?" +
                " and number=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(controllerPhysicalId);
        parameters.add(slotNumber);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectFacilitySlots(SQLConnection connection, int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectFacilitySlots: facilityId=" + facilityId);
        String sql = "select controller_id, physical_id as controller_physical_id, slot.id, number, parked, begin_ts as parked_ts, reserved, reservation.id as reservation_id, reservation_ts, email" +
                " from facility inner join controller on facility.id=controller.facility_id" +
                " inner join slot on controller.id=slot.controller_id" +
                " left join reservation on reservation.slot_id=slot.id" +
                " left join user on reservation.user_id=user.id" +
                " left join transaction on transaction.reservation_id=reservation.id" +
                " where facility.id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(facilityId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservableSlots(SQLConnection connection, int facilityId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservableSlots: facilityId=" + facilityId);
        String sql = "select slot.id as id, slot.number, controller.facility_id, slot.controller_id" +
                " from controller inner join slot on controller.id=slot.controller_id" +
                " where controller.facility_id=?" +
                " and controller.available=1" +
                " and slot.parked=0" +
                " and slot.reserved=0";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(facilityId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservation(SQLConnection connection, int reservationId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservation: reservationId=" + reservationId);
        String sql = "select reservation.id as id, reservation_ts, confirmation_no, user_id, slot_id, slot.number as slot_no" +
                " from reservation inner join slot on reservation.slot_id=slot.id" +
                " where reservation.id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reservationId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservationByConfirmationNumber(SQLConnection connection, int confirmationNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservationByConfirmationNumber: confirmationNumber=" + confirmationNumber);
        String sql = "select reservation.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name, reservation.fee, reservation.fee_unit, reservation.grace_period" +
                " from reservation inner join slot on reservation.slot_id=slot.id" +
                " inner join controller on controller.id=slot.controller_id" +
                " inner join facility on facility.id=controller.facility_id" +
                " inner join user on user.id = user_id" +
                " where confirmation_no=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(confirmationNumber);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservationByUserId(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservationByUserId: userId=" + userId);
        String sql = "select reservation.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name, reservation.fee, reservation.fee_unit, reservation.grace_period" +
                " from reservation inner join slot on reservation.slot_id=slot.id" +
                " inner join controller on controller.id=slot.controller_id" +
                " inner join facility on facility.id=controller.facility_id" +
                " inner join user on user.id=user_id" +
                " where user_id=? and reservation.activiate=1" +
                " order by reservation_ts";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateFacility(SQLConnection connection, int facilityId, String name, double fee, int feeUnit, int gracePeriod, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateFacility: facilityId=" + facilityId + ", name=" + name + ", fee=" + fee + ", feeUnit=" + feeUnit + ", gracePeriod=" + gracePeriod);
        String sql = "update facility set name=?, fee=?, fee_unit=?, grace_period=? where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(name);
        parameters.add(fee);
        parameters.add(feeUnit);
        parameters.add(gracePeriod);
        parameters.add(facilityId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateSlotParked(SQLConnection connection, int slotId, boolean parked, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateSlotParked: slotId=" + slotId + ", parked=" + parked);
        String sql = "update slot set parked=? where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(parked ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateSlotReserved(SQLConnection connection, int slotId, boolean reserved, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "update slot set reserved=? where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reserved ? 1 : 0);
        parameters.add(slotId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateControllerAvailable(SQLConnection connection, String controllerPhysicalId, boolean available, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateControllerAvailable: controllerPhysicalId=" + controllerPhysicalId + ", available=" + available);
        String sql = "update controller set available=? where physical_id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(available ? 1 : 0);
        parameters.add(controllerPhysicalId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void deleteReservation(SQLConnection connection, int reservationId, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "delete from reservation where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray().add(reservationId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertReservation(SQLConnection connection, int userId, int slotId, int reservationTs, int confirmationNo, double fee, int feeUnit, int gracePeriod, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into reservation(user_id,slot_id,reservation_ts,confirmation_no, fee, fee_unit, grace_period) values (?,?,?,?,?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        parameters.add(slotId);
        parameters.add(reservationTs);
        parameters.add(confirmationNo);
        parameters.add(fee);
        parameters.add(feeUnit);
        parameters.add(gracePeriod);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertTransaction(SQLConnection connection, int reservationId, int beginTs, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into transaction(reservation_id,begin_ts) values (?,?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reservationId);
        parameters.add(beginTs);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertUser(SQLConnection connection, String email, String password, String cardNumber, String cardExpiration, int userType, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into user(email,password,card_number,card_expiration,type) values(?,?,?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(email);
        parameters.add(password);
        parameters.add(cardNumber);
        parameters.add(cardExpiration);
        parameters.add(userType);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertSession(SQLConnection connection, int userId, String sessionKey, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into session(user_id,session_key) values(?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        parameters.add(sessionKey);
        updateWithParams(connection, sql, parameters, resultHandler);
    }
}