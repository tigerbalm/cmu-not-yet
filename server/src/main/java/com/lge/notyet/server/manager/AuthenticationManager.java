package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.exception.SureParkException;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.UUID;

public class AuthenticationManager {
    private static AuthenticationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;

    private AuthenticationManager() {
        logger = LoggerFactory.getLogger(AuthenticationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
    }

    public static AuthenticationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new AuthenticationManager();
            }
            return instance;
        }
    }

    private static String createSessionKey() {
        return UUID.randomUUID().toString().substring(0, 30);
    }

    public void signUp(String email, String password, String cardNumber, String cardExpiration, int userType, Handler<AsyncResult<String>> handler) {
        logger.info("signUp: email=" + email + ", password=" + password + ", cardNumber=" + cardNumber + ", cardExpiration=" + cardExpiration + ", userType=" + userType);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectUserByEmail(sqlConnection, email, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                    } else {
                        final boolean existentUser = ar2.result().size() > 0;
                        if (existentUser) {
                            handler.handle(Future.failedFuture(SureParkException.createExistentUserException()));
                            databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                        } else {
                            databaseProxy.insertUser(sqlConnection, email, password, cardNumber, cardExpiration, userType, ar3 -> {
                                if (ar3.failed()) {
                                    handler.handle(Future.failedFuture(ar3.cause()));
                                    databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                                } else {
                                    final String sessionKey = createSessionKey();
                                    final int userId = ar3.result().get(0).asInt();
                                    databaseProxy.insertSession(sqlConnection, userId, sessionKey, ar4 -> {
                                        if (ar4.failed()) {
                                            handler.handle(Future.failedFuture(ar4.cause()));
                                            databaseProxy.closeConnection(sqlConnection, false, ar -> {});
                                        } else {
                                            handler.handle(Future.succeededFuture(sessionKey));
                                            databaseProxy.closeConnection(sqlConnection, true, ar -> {});
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void getSessionUser(String sessionKey, Handler<AsyncResult<JsonObject>> handler) {
        logger.debug("getSessionUser: sessionKey=" + sessionKey);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectUser(sqlConnection, sessionKey, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {});
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        if (userObjects.size() != 1) {
                            handler.handle(Future.failedFuture(SureParkException.createInvalidSessionException()));
                            databaseProxy.closeConnection(sqlConnection, ar4 -> {});
                        } else {
                            handler.handle(Future.succeededFuture(userObjects.get(0)));
                            databaseProxy.closeConnection(sqlConnection, ar5 -> {});
                        }
                    }
                });
            }
        });
    }

    public void getEmailPasswordUser(String email, String password, Handler<AsyncResult<JsonObject>> handler) {
        logger.debug("getEmailPasswordUser: email=" + email + ", password=" + password);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectUser(sqlConnection, email, password, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {});
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        if (userObjects.size() != 1) {
                            handler.handle(Future.failedFuture(SureParkException.createInvalidEmailPasswordException()));
                            databaseProxy.closeConnection(sqlConnection, ar4 -> {});
                        } else {
                            handler.handle(Future.succeededFuture(userObjects.get(0)));
                            databaseProxy.closeConnection(sqlConnection, ar5 -> {});
                        }
                    }
                });
            }
        });
    }

    public void checkUserType(String sessionKey, int userType, Handler<AsyncResult<Void>> handler) {
        logger.debug("checkUserType: sessionKey=" + sessionKey + ", userType=" + userType);
        getSessionUser(sessionKey, ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                final JsonObject userObject = ar.result();
                if (userObject.get("type").asInt() != userType) {
                    handler.handle(Future.failedFuture(SureParkException.createNoAuthorizationException()));
                } else {
                    handler.handle(Future.succeededFuture());
                }
            }
        });
    }
}