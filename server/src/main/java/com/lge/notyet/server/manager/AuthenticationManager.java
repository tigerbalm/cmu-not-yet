package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.server.exception.*;
import com.lge.notyet.server.proxy.CreditCardProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import com.lge.notyet.server.security.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AuthenticationManager {
    private static AuthenticationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CreditCardProxy creditCardProxy;

    private AuthenticationManager() {
        logger = LoggerFactory.getLogger(AuthenticationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        creditCardProxy = CreditCardProxy.getInstance();
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

    public void signUp(String email, String password, String cardNumber, String cardExpiration, Handler<AsyncResult<Void>> handler) {
        logger.info("signUp: email=" + email + ", password=" + password + ", cardNumber=" + cardNumber + ", cardExpiration=" + cardExpiration);
        creditCardProxy.verify(cardNumber, cardExpiration, ar0 -> {
            if (ar0.failed()) {
                handler.handle(Future.failedFuture(ar0.cause()));
            } else {
                final boolean verified = ar0.result();
                if (!verified) {
                    handler.handle(Future.failedFuture(new InvalidCardInformationException()));
                } else {
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
                                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(new ExistentUserException())));
                                    } else {
                                        databaseProxy.insertUser(sqlConnection, email, password, cardNumber, cardExpiration, ar3 -> {
                                            if (ar3.failed()) {
                                                databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar3.cause())));
                                            } else {
                                                databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    public void getSession(String email, String password, Handler<AsyncResult<Session>> handler) {
        logger.debug("getSession: email=" + email + ", password=" + password);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.selectUser(sqlConnection, email, password, ar2 -> {
                    if (ar2.failed()) {
                        handler.handle(Future.failedFuture(ar2.cause()));
                        databaseProxy.closeConnection(sqlConnection, ar3 -> {
                        });
                    } else {
                        List<JsonObject> userObjects = ar2.result();
                        if (userObjects.size() != 1) {
                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(new InvalidEmailPasswordException())));
                        } else {
                            final JsonObject userObject = userObjects.get(0);
                            final int userId = userObject.get("id").asInt();

                            databaseProxy.deleteSession(sqlConnection, userId, ar4 -> {
                                if (ar4.failed()) {
                                    databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(ar4.cause())));
                                } else {
                                    String sessionKey = createSessionKey();
                                    int issueTs = (int) Instant.now().getEpochSecond();
                                    databaseProxy.insertSession(sqlConnection, userId, sessionKey, issueTs, ar5 -> {
                                        if (ar5.failed()) {
                                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.failedFuture(ar5.cause())));
                                        } else {
                                            final Session session = new Session(sessionKey, userObject);
                                            databaseProxy.closeConnection(sqlConnection, ar -> handler.handle(Future.succeededFuture(session)));
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

    public void getSession(String sessionKey, Handler<AsyncResult<Session>> handler) {
        logger.debug("getSession: sessionKey=" + sessionKey);
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
                            databaseProxy.closeConnection(sqlConnection, ar4 -> handler.handle(Future.failedFuture(new InvalidSessionException())));
                        } else {
                            final JsonObject userObject = userObjects.get(0);
                            final Session session = new Session(sessionKey, userObject);
                            databaseProxy.closeConnection(sqlConnection, ar5 -> handler.handle(Future.succeededFuture(session)));
                        }
                    }
                });
            }
        });
    }

    public void invalidateSession(String sessionKey, Handler<AsyncResult<Session>> handler) {
        logger.debug("invalidateSession: sessionKey=" + sessionKey);
        databaseProxy.openConnection(ar1 -> {
            if (ar1.failed()) {
                handler.handle(Future.failedFuture(ar1.cause()));
            } else {
                final SQLConnection sqlConnection = ar1.result();
                databaseProxy.deleteSession(sqlConnection, sessionKey, ar2 -> {
                    if (ar2.failed()) {
                        databaseProxy.closeConnection(sqlConnection, false, ar -> handler.handle(Future.failedFuture(ar2.cause())));
                    } else {
                        databaseProxy.closeConnection(sqlConnection, true, ar -> handler.handle(Future.succeededFuture()));
                    }
                });
            }
        });
    }
}