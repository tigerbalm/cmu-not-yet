package com.lge.notyet.server.proxy;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.model.Statistics;
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
            connection.commit(ar -> connection.close(ar2 -> handler.handle(ar)));
        } else {
            connection.rollback(ar -> connection.close(ar2 -> handler.handle(ar)));
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

    public void rawQuery(SQLConnection connection, String sql, Handler<AsyncResult<Statistics>> resultHandler) {
        connection.query(sql, ar -> resultHandler.handle(new AsyncResult<Statistics>() {
            @Override
            public Statistics result() {
                List<String> colunmnameList = ar.result().getColumnNames();
                List<JsonArray> valuesList = ar.result().getResults().stream().map(row -> JsonArray.readFrom(row.toString())).collect(Collectors.toList());
                return new Statistics(colunmnameList, valuesList);
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
                    result.add(keys.getInteger(i));
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
        queryWithParams(connection, "select user.id, email, password, card_number, card_expiration," +
                " read_facility, write_facility, read_reservation, write_reservation, read_statistics" +
                " from user inner join role on role.id=user.role_id" +
                " where email=? and password=password(?)", parameters, resultHandler);
    }

    public void selectSession(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectSession: userId=" + userId);
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        queryWithParams(connection, "select * from session where user_id=?", parameters, resultHandler);
    }

    public void selectUser(SQLConnection connection, String sessionKey, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectUser: sessionKey=" + sessionKey);
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(sessionKey);
        queryWithParams(connection, "select session_key, user.id, email, password, card_number, card_expiration," +
                " read_facility, write_facility, read_reservation, write_reservation, read_statistics" +
                " from user inner join role on role.id=user.role_id" +
                " inner join session on session.user_id=user.id" +
                " where session_key=?", parameters, resultHandler);
    }

    public void selectUser(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        queryWithParams(connection, "select * from user where user.id=?", parameters, resultHandler);
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
        String sql = "select activated, controller_id, physical_id as controller_physical_id, controller.available, slot.id, number, parked, begin_ts as parked_ts, reserved, ar.id as reservation_id, reservation_ts, email\n" +
                "from facility inner join controller on facility.id=controller.facility_id\n" +
                "inner join slot on controller.id=slot.controller_id\n" +
                "left join (select * from reservation where activated=1) as ar on ar.slot_id=slot.id\n" +
                "left join user on ar.user_id=user.id\n" +
                "left join transaction on transaction.reservation_id=ar.id\n" +
                "where facility.id=?";
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
        String sql = "select reservation.id, slot.id as slot_id, slot.number as slot_number, fee, fee_unit, expiration_ts, begin_ts, end_ts, revenue, controller.physical_id as controller_physical_id" +
                " from reservation inner join slot on reservation.slot_id=slot.id" +
                " inner join controller on slot.controller_id=controller.id" +
                " left join transaction on transaction.reservation_id=reservation.id" +
                " where reservation.id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reservationId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectActivatedReservation(SQLConnection connection, int slotId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectActivatedReservation: slotId=" + slotId);
        String sql = "select activated, reservation.id, slot.id as slot_id, slot.number as slot_number, fee, fee_unit, expiration_ts, begin_ts, end_ts, revenue, controller.physical_id as controller_physical_id" +
                " from reservation inner join slot on reservation.slot_id=slot.id" +
                " inner join controller on slot.controller_id=controller.id" +
                " left join transaction on transaction.reservation_id=reservation.id" +
                " where activated=1 and slot_id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(slotId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservation(SQLConnection connection, String controllerPhysicalId, int slotNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservation: controllerPhysicalId=" + controllerPhysicalId + ", slotNumber=" + slotNumber);
        String sql = "select ar.id, ar.user_id, slot.id as slot_id, fee, fee_unit, expiration_ts, begin_ts" +
                " from (select * from reservation where activated=1) as ar" +
                " inner join slot on ar.slot_id=slot.id" +
                " inner join controller on slot.controller_id=controller.id" +
                " left join transaction on transaction.reservation_id=ar.id" +
                " where physical_id=? and slot.number=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(controllerPhysicalId);
        parameters.add(slotNumber);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectExpiredReservation(SQLConnection connection, int currentTs, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservation: currentTs=" + currentTs);
        String sql = "select *" +
                " from reservation" +
                " where activated=1 and expiration_ts<=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(currentTs);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectReservationByConfirmationNumber(SQLConnection connection, int confirmationNumber, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectReservationByConfirmationNumber: confirmationNumber=" + confirmationNumber);
        String sql = "select ar.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name, ar.fee, ar.fee_unit, ar.expiration_ts" +
                " from (select * from reservation where activated=1) as ar inner join slot on ar.slot_id=slot.id" +
                " inner join controller on controller.id=slot.controller_id" +
                " inner join facility on facility.id=controller.facility_id" +
                " inner join user on user.id = user_id" +
                " where confirmation_no=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(confirmationNumber);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectActivatedReservationByUserId(SQLConnection connection, int userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectActivatedReservationByUserId: userId=" + userId);
        String sql = "select ar.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name, ar.fee, ar.fee_unit, ar.expiration_ts, begin_ts, end_ts" +
                " from (select * from reservation where activated=1) as ar inner join slot on ar.slot_id=slot.id" +
                " inner join controller on controller.id=slot.controller_id" +
                " inner join facility on facility.id=controller.facility_id" +
                " inner join user on user.id=user_id" +
                " left join transaction on transaction.reservation_id=ar.id" +
                " where user_id=?" +
                " order by reservation_ts";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        queryWithParams(connection, sql, parameters, resultHandler);
    }

    public void selectActivatedReservations(SQLConnection connection, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        logger.info("selectActivatedReservations:");
        String sql = "select ar.id as id, reservation_ts, confirmation_no, user_id, user.email as user_email, slot_id, slot.number as slot_no, controller_id, physical_id as controller_physical_id, facility_id, facility.name as facility_name, ar.fee, ar.fee_unit, ar.expiration_ts, begin_ts, end_ts" +
                " from (select * from reservation where activated=1) as ar inner join slot on ar.slot_id=slot.id" +
                " inner join controller on controller.id=slot.controller_id" +
                " inner join facility on facility.id=controller.facility_id" +
                " inner join user on user.id=user_id" +
                " left join transaction on transaction.reservation_id=ar.id" +
                " order by reservation_ts";
        query(connection, sql, resultHandler);
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

    public void insertReservation(SQLConnection connection, int userId, int slotId, int reservationTs, int confirmationNo, double fee, int feeUnit, int expirationTs, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into reservation(user_id,slot_id,reservation_ts,confirmation_no, fee, fee_unit, expiration_ts) values (?,?,?,?,?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        parameters.add(slotId);
        parameters.add(reservationTs);
        parameters.add(confirmationNo);
        parameters.add(fee);
        parameters.add(feeUnit);
        parameters.add(expirationTs);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertTransaction(SQLConnection connection, int reservationId, int beginTs, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("insertTransaction: reservationId=" + reservationId + ", beginTs=" + beginTs);
        String sql = "insert into transaction(reservation_id,begin_ts) values (?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(reservationId);
        parameters.add(beginTs);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateReservationActivated(SQLConnection connection, int reservationId, boolean activated, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateTransaction: reservationId=" + reservationId + ", activated=" + activated);
        String sql = "update reservation set activated=? where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(activated ? 1 : 0);
        parameters.add(reservationId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateReservationSlot(SQLConnection connection, int reservationId, int slotId,  Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateReservationSlot: reservationId=" + reservationId + ", slotId=" + slotId);
        String sql = "update reservation set slot_id=? where id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(slotId);
        parameters.add(reservationId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void updateTransaction(SQLConnection connection, int reservationIdId, int endTs, double revenue, long paymentId, Handler<AsyncResult<JsonArray>> resultHandler) {
        logger.info("updateTransaction: reservationId=" + reservationIdId + ", endTs=" + endTs + ", revenue=" + revenue);
        String sql = "update transaction set end_ts=?, revenue=?, payment_id=? where reservation_id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(endTs);
        parameters.add(revenue);
        parameters.add(paymentId);
        parameters.add(reservationIdId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertUser(SQLConnection connection, String email, String password, String cardNumber, String cardExpiration, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into user(email,password,card_number,card_expiration,role_id) values(?,password(?),?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(email);
        parameters.add(password);
        parameters.add(cardNumber);
        parameters.add(cardExpiration);
        parameters.add(4); // role_id 4 means driver
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void insertSession(SQLConnection connection, int userId, String sessionKey, int issueTs, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "insert into session(user_id,session_key,issue_ts) values(?,?,?)";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        parameters.add(sessionKey);
        parameters.add(issueTs);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void deleteSession(SQLConnection connection, String sessionKey, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "delete from session where session_key=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(sessionKey);
        updateWithParams(connection, sql, parameters, resultHandler);
    }

    public void deleteSession(SQLConnection connection, int userId, Handler<AsyncResult<JsonArray>> resultHandler) {
        String sql = "delete from session where user_id=?";
        io.vertx.core.json.JsonArray parameters = new io.vertx.core.json.JsonArray();
        parameters.add(userId);
        updateWithParams(connection, sql, parameters, resultHandler);
    }
}