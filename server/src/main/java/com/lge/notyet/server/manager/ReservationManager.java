package com.lge.notyet.server.manager;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.channels.ReservationResponseChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Random;

public class ReservationManager {
    private static ReservationManager instance = null;

    private final Logger logger;
    private final DatabaseProxy databaseProxy;
    private final CommunicationProxy communicationProxy;

    private ReservationManager() {
        logger = LoggerFactory.getLogger(ReservationManager.class);
        databaseProxy = DatabaseProxy.getInstance(null);
        communicationProxy = CommunicationProxy.getInstance(null);

        INetworkConnection networkConnection = communicationProxy.getNetworkConnection();
        new ReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> reserve(uri, message)).listen();
        // new ReservationRequestChannel(networkConnection, 1).request(ReservationRequestChannel.createRequestMessage("ssssss", System.currentTimeMillis()));
    }

    public static ReservationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new ReservationManager();
            }
            return instance;
        }
    }

    private void reserve(Uri uri, NetworkMessage message) {
        final String sessionKey = ReservationRequestChannel.getSessionKey(message);
        final int facilityId = ReservationRequestChannel.getFacilityId(uri);
        final long reservationTimestamp = ReservationRequestChannel.getReservationTimestamp(message);

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
                    final int userId = userObject.get("id").asInt();
                    // 2. facilityId -> reservable slots
                    databaseProxy.selectReservableSlots(facilityId, ar2 -> {
                        if (!ar2.succeeded()) {
                            ar2.cause().printStackTrace();
                            communicationProxy.responseServerError(message);
                        } else {
                            final List<JsonObject> slots = ar2.result();
                            if (slots.size() == 0) {
                                communicationProxy.responseFail(message, "NO_AVAILABLE_SLOT");
                            } else {
                                final int slotId = slots.get(0).get("id").asInt();
                                final int confirmationNumber = new Random().nextInt((999999 - 100000) + 1) + 100000;
                                // 3. make a reservation
                                databaseProxy.insertReservation(userId, slotId, reservationTimestamp, confirmationNumber, ar3 -> {
                                    if (!ar3.succeeded()) {
                                        ar3.cause().printStackTrace();
                                        communicationProxy.responseServerError(message);
                                        databaseProxy.rollback(v -> {
                                        });
                                    } else {
                                        // 4. set slot status reserved
                                        databaseProxy.updateSlotReserved(slotId, true, ar4 -> {
                                            if (!ar4.succeeded()) {
                                                ar4.cause().printStackTrace();
                                                communicationProxy.responseServerError(message);
                                            } else {
                                                // 5. commit
                                                databaseProxy.commit(ar5 -> {
                                                    if (!ar5.succeeded()) {
                                                        ar5.cause().printStackTrace();
                                                        communicationProxy.responseServerError(message);
                                                    } else {
                                                        // 6. get the reservation and response
                                                        databaseProxy.selectReservation(confirmationNumber, ar6 -> {
                                                            if (!ar6.succeeded()) {
                                                                ar6.cause().printStackTrace();
                                                                communicationProxy.responseServerError(message);
                                                            } else {
                                                                final List<JsonObject> reservationObjects = ar6.result();
                                                                if (reservationObjects.size() != 1) {
                                                                    communicationProxy.responseServerError(message);
                                                                } else {
                                                                    final JsonObject reservationObject = reservationObjects.get(0);
                                                                    if (reservationObjects == null) {
                                                                        communicationProxy.responseServerError(message);
                                                                    } else {
                                                                        communicationProxy.responseSuccess(message, reservationObject);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}