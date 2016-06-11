package com.lge.notyet.server.proxy;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
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
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                sqlConnection = res.result();
                logger.info("database connected");
                future.complete();
            } else {
                future.fail(res.cause());
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

    private Future<JsonObject> singleItemReturnQuery(String sql) {
        Future<JsonObject> future = Future.future();
        sqlConnection.query(sql, ar -> {
            if (ar.succeeded()) {
                ResultSet resultSet = ar.result();
                JsonObject result = resultSet.getRows().get(0);
                logger.info("singleItemReturnQuery(): sql=" + sql + "result=" + result);
                future.complete(result);
            } else {
                logger.error("singleItemReturnQuery(): sql=" + sql, ar.cause());
                future.fail(ar.cause());
            }
        });
        return future;
    }

    private Future<List<JsonObject>> multipleItemReturnQuery(String sql) {
        Future<List<JsonObject>> future = Future.future();
        System.out.println(sql);
        sqlConnection.query(sql, ar -> {
            if (ar.succeeded()) {
                ResultSet resultSet = ar.result();
                future.complete(resultSet.getRows());
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }

    public Future<JsonObject> getUser(int id) {
        return singleItemReturnQuery("select * from user where id = " + id);
    }

    public Future<JsonObject> getUser(String email, String password) {
        return singleItemReturnQuery("select * from user where email=\'" + email + "\' and password=\'" + password + "\'");
    }

    public Future<JsonObject> getUser(String sessionKey) {
        return singleItemReturnQuery("select * from user, session where session_key=\'" + sessionKey + "\'");
    }
}