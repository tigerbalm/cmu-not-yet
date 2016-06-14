package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.channels.LoginResponseChannel;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class AuthenticationManager {
    private static AuthenticationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;

    private AuthenticationManager() {
        logger = LoggerFactory.getLogger(AuthenticationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new LoginResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> login(uri, message)).listen();
        // new LoginRequestChannel(networkConnection).request(LoginRequestChannel.createRequestMessage("reshout@gmail.com", "password"));
    }

    public static AuthenticationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new AuthenticationManager();
            }
            return instance;
        }
    }

    private void login(Uri uri, NetworkMessage message) {
        final String email = LoginRequestChannel.getEmail(message);
        final String password = LoginRequestChannel.getPassword(message);

        // 1. email, password -> userObject
        databaseProxy.selectUser(email, password, ar1 -> {
            if (!ar1.succeeded()) {
                ar1.cause().printStackTrace();
                communicationProxy.responseServerError(message);
            } else {
                final List<JsonObject> userObjects = ar1.result();
                if (userObjects.isEmpty()) {
                    communicationProxy.responseFail(message, "INVALID_EMAIL_PASSWORD");
                } else {
                    final JsonObject userObject = userObjects.get(0);
                    final int userId = userObject.get("id").asInt();
                    final int userType = userObject.get("type").asInt();

                    // 2. userId -> session
                    databaseProxy.selectSession(userId, ar2 -> {
                        if (!ar2.succeeded()) {
                            ar2.cause().printStackTrace();
                            communicationProxy.responseServerError(message);
                        } else {
                            final List<JsonObject> sessionObjects = ar2.result();
                            if (sessionObjects.isEmpty()) {
                                communicationProxy.responseServerError(message);
                            } else {
                                final JsonObject sessionObject = sessionObjects.get(0);
                                final String sessionKey = sessionObject.get("session_key").asString();
                                String cardNumber = null;
                                String cardExpiration = null;
                                if (userType == User.USER_TYPE_DRIVER) {
                                    cardNumber = userObject.get("card_number").asString();
                                    cardExpiration = userObject.get("card_expiration").asString();
                                }
                                final JsonObject responseObject = LoginResponseChannel.createResponseOjbect(userId, userType, cardNumber, cardExpiration, sessionKey);
                                communicationProxy.responseSuccess(message, responseObject);
                            }
                        }
                    });
                }
            }
        });
    }
}
