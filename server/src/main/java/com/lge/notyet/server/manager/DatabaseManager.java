package com.lge.notyet.server.manager;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

public class DatabaseManager {
    private static DatabaseManager instance = null;

    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "dba";
    private static final String PASSWORD = "dba";
    private static final String DATABASE = "sure-park";

    private Vertx vertx;
    private AsyncSQLClient sqlClient;
    private SQLConnection sqlConnection;

    private DatabaseManager(Vertx vertx) {
        this.vertx = vertx;
    }

    public static DatabaseManager getInstance(Vertx vertx) {
        synchronized (DatabaseManager.class) {
            if (instance == null) {
                instance = new DatabaseManager(vertx);
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
}