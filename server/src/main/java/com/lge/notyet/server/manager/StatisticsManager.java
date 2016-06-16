package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonArray;
import com.lge.notyet.channels.GetDBQueryRequestChannel;
import com.lge.notyet.channels.GetDBQueryResponseChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
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
    private final CommunicationProxy communicationProxy;
    private final AuthenticationManager authenticationManager;

    private StatisticsManager() {
        logger = LoggerFactory.getLogger(StatisticsManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);
        authenticationManager = AuthenticationManager.getInstance();

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();

        new GetDBQueryResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getStatistics(message)).listen();
        // new GetDBQueryRequestChannel(networkConnection).request(GetDBQueryRequestChannel.createRequestMessage("session1", "select email, id from user"));
    }

    public static StatisticsManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new StatisticsManager();
            }
            return instance;
        }
    }

    private void getStatisticsByQuery(String query, Handler<AsyncResult<List<JsonArray>>> handler) {
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
                        List<JsonArray> objects = ar2.result();
                        handler.handle(Future.succeededFuture(objects));
                        databaseProxy.closeConnection(sqlConnection, ar4 -> {
                        });
                    }
                });
            }
        });
    }

    private void getStatistics(NetworkMessage message) {
        final String sessionKey = GetDBQueryRequestChannel.getSessionKey(message);
        final String query = GetDBQueryRequestChannel.getKeyDbqueryKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_OWNER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                getStatisticsByQuery(query, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        communicationProxy.responseSuccess(message, GetDBQueryResponseChannel.createResponseObject(ar2.result()));
                    }
                });
            }
        });
    }
}
