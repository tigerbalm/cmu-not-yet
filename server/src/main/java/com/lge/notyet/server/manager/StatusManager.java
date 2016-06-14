package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.ReservableFacilitiesRequestChannel;
import com.lge.notyet.channels.ReservableFacilitiesResponseChannel;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

public class StatusManager {
    private static StatusManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;

    private StatusManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new ReservableFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservableFacilities(message)).listen();
        new ReservableFacilitiesRequestChannel(networkConnection).request(ReservableFacilitiesRequestChannel.createRequestMessage("ssssss"));
    }

    public static StatusManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new StatusManager();
            }
            return instance;
        }
    }

    public void getReservableFacilities(NetworkMessage message) {
        final String sessionKey = ReservationRequestChannel.getSessionKey(message);
        // 1. sessionKey -> userObject
        databaseProxy.selectUser(sessionKey, ar1 -> {
            if (!ar1.succeeded()) {
                ar1.cause().printStackTrace();
                communicationProxy.responseServerError(message);
            } else {
                final List<JsonObject> userObjects = ar1.result();
                if (userObjects.isEmpty()) {
                    communicationProxy.responseFail(message, "INVALID_SESSION");
                } else {
                    final JsonObject userObject = userObjects.get(0);
                    final int userType = userObject.get("type").asInt();
                    if (userType != User.USER_TYPE_DRIVER) {
                        communicationProxy.responseFail(message, "NOT_DRIVER");
                    } else {
                        // 2. get reservable facilities
                        databaseProxy.selectReservableFacilities(ar2 -> {
                            if (!ar2.succeeded()) {
                                ar2.cause().printStackTrace();
                                communicationProxy.responseServerError(message);
                            } else {
                                List<JsonObject> facilityObjects = ar2.result();
                                communicationProxy.responseSuccess(message, ReservableFacilitiesResponseChannel.createResponseObject(facilityObjects));
                            }
                        });
                    }
                }
            }
        });
    }
}
