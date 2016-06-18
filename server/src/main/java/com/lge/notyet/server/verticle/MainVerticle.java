package com.lge.notyet.server.verticle;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.server.manager.AuthenticationManager;
import com.lge.notyet.server.manager.ReservationManager;
import com.lge.notyet.server.manager.FacilityManager;
import com.lge.notyet.server.manager.StatisticsManager;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

public class MainVerticle extends AbstractVerticle {
    private static final String BROKER_HOST = "localhost";
    private static final String DB_HOST = "localhost";
    private static final String DB_USERNAME = "dba";
    private static final String DB_PASSWORD = "dba";

    private Logger logger;
    private AuthenticationManager authenticationManager;
    private ReservationManager reservationManager;
    private FacilityManager facilityManager;
    private StatisticsManager statisticsManager;

    private DatabaseProxy databaseProxy;
    private CommunicationProxy communicationProxy;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        logger = LoggerFactory.getLogger(MainVerticle.class);
        communicationProxy = CommunicationProxy.getInstance(vertx);
        databaseProxy = DatabaseProxy.getInstance(vertx);

        Future<Void> communicationReady = communicationProxy.start(BROKER_HOST);
        Future<Void> databaseReady = databaseProxy.start(DB_HOST, DB_USERNAME, DB_PASSWORD);
        CompositeFuture.all(communicationReady, databaseReady).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
                startManagers();
                listenChannels();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private void startManagers() {
        authenticationManager = AuthenticationManager.getInstance();
        reservationManager = ReservationManager.getInstance();
        facilityManager = FacilityManager.getInstance();
        statisticsManager = StatisticsManager.getInstance();
    }

    private void listenChannels() {
        final INetworkConnection networkConnection = communicationProxy.getNetworkConnection();

        new SignUpResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> signUp(message)).listen();
        new LoginResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> login(message)).listen();
        new ReservableFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservableFacilities(message)).listen();
        new UpdateSlotStatusSubscribeChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateSlotStatus(uri, message)).listen();
        new GetFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getFacilities(uri, message)).listen();
        new GetSlotsResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getSlots(uri, message)).listen();
    }

    private void login(NetworkMessage message) {
        final String email = LoginRequestChannel.getEmail(message);
        final String password = LoginRequestChannel.getPassword(message);

        authenticationManager.getEmailPasswordUser(email, password, ar -> {
            if (ar.failed()) {
                communicationProxy.responseFail(message, ar.cause());
            } else {
                JsonObject userObject = ar.result();
                communicationProxy.responseSuccess(message, userObject);
            }
        });
    }

    private void signUp(NetworkMessage message) {
        final String email = SignUpRequestChannel.getEmail(message);
        final String password = SignUpRequestChannel.getPassword(message);
        final String cardNumber = SignUpRequestChannel.getCardNumber(message);
        final String cardExpiration = SignUpRequestChannel.getCardExpiration(message);

        authenticationManager.signUp(email, password, cardNumber, cardExpiration, User.USER_TYPE_DRIVER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                communicationProxy.responseSuccess(message);
            }
        });
    }

    private void getReservableFacilities(NetworkMessage message) {
        final String sessionKey = ReservationRequestChannel.getSessionKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_DRIVER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                facilityManager.getReservableFacilities(ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        List<JsonObject> facilityObjects = ar2.result();
                        communicationProxy.responseSuccess(message, ReservableFacilitiesResponseChannel.createResponseObject(facilityObjects));
                    }
                });
            }
        });
    }

    private void updateSlotStatus(Uri uri, NetworkMessage message) {
        final String controllerPhysicalId = UpdateSlotStatusPublishChannel.getControllerPhysicalId(uri);
        final int slotNumber = UpdateSlotStatusPublishChannel.getSlotNumber(uri);
        final boolean occupied = UpdateSlotStatusPublishChannel.isOccupied(message);

        facilityManager.getSlot(controllerPhysicalId, slotNumber, ar1 -> {
            if (ar1.failed()) {
                ar1.cause().printStackTrace();
            } else {
                final JsonObject slotObject = ar1.result();
                final int slotId = slotObject.get("id").asInt();
                facilityManager.updateSlotOccupied(slotId, occupied, ar2 -> {
                    if (ar2.failed()) {
                        ar2.cause().printStackTrace();
                    } else {
                        logger.info("updateSlotStatus: slot=" + slotObject + " updated occupied=" + occupied);
                    }
                });
            }
        });
    }

    private void getFacilities(Uri uri, NetworkMessage message) {
        final String sessionKey = GetFacilitiesRequestChannel.getSessionKey(message);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final int userId = ar1.result().get("id").asInt();
                facilityManager.getFacilities(userId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        communicationProxy.responseSuccess(message, GetFacilitiesResponseChannel.createResponseObject(ar2.result()));
                    }
                });
            }
        });
    }

    private void getSlots(Uri uri, NetworkMessage message) {
        final String sessionKey = GetSlotsRequestChannel.getSessionKey(message);
        final int facilityId = GetSlotsRequestChannel.getFacilityId(uri);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userId = userObject.get("id").asInt();
                final int userType = userObject.get("type").asInt();

                if (userType != User.USER_TYPE_ATTENDANT) {
                    communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                } else {
                    facilityManager.getFacilities(userId, ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            final List<JsonObject> facilityObjectList = ar2.result();
                            boolean isAttendantFacility = false;
                            for (JsonObject facilityObject : facilityObjectList) {
                                if (facilityObject.get("id").asInt() == facilityId) {
                                    isAttendantFacility = true;
                                    break;
                                }
                            }
                            if (!isAttendantFacility) {
                                communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                            } else {
                                facilityManager.getFacilitySlots(facilityId, ar3 -> {
                                    if (ar3.failed()) {
                                        communicationProxy.responseFail(message, ar3.cause());
                                    } else {
                                        communicationProxy.responseSuccess(message, GetSlotsResponseChannel.createResponseObject(ar3.result()));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        CompositeFuture.all(communicationProxy.stop(), databaseProxy.stop()).setHandler(ar -> {
            if (ar.succeeded()) {
                stopFuture.complete();
            } else {
                stopFuture.fail(ar.cause());
            }
        });
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getCanonicalName());
    }
}