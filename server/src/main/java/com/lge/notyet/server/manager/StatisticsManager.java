package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonArray;
import com.lge.notyet.server.model.Statistics;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

public class StatisticsManager {
    private static StatisticsManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private StatisticsManager() {
        logger = LoggerFactory.getLogger(StatisticsManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
    }

    public static StatisticsManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new StatisticsManager();
            }
            return instance;
        }
    }

    public void getStatisticsByQuery(String query, Handler<AsyncResult<Statistics>> handler) {
        logger.info("getStatisticsByQuery: query=" + query);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.rawQuery(sqlConnection, query, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        Statistics statistics = ar2.result();
                        handler.handle(Future.succeededFuture(statistics));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
                    }
                });
            }
        });
    }
}
