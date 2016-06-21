package com.lge.notyet.server.verticle;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.server.manager.AuthenticationManager;
import com.lge.notyet.server.manager.ReservationManager;
import com.lge.notyet.server.manager.FacilityManager;
import com.lge.notyet.server.manager.StatisticsManager;
import com.lge.notyet.server.model.Statistics;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Random;

import static com.lge.notyet.server.manager.ReservationManager.*;

public class MainVerticle extends AbstractVerticle {
    private static final String BROKER_HOST = "192.168.1.21";
    private static final boolean REDUNDANCY = true;
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
        communicationProxy = CommunicationProxy.getInstance();
        databaseProxy = DatabaseProxy.getInstance(vertx);

        Future<Void> communicationReady = communicationProxy.start(BROKER_HOST, REDUNDANCY);
        Future<Void> databaseReady = databaseProxy.start(DB_HOST, DB_USERNAME, DB_PASSWORD);
        CompositeFuture.all(communicationReady, databaseReady).setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
                startManagers();
                registerListeners();
                listenChannels();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private void startManagers() {
        authenticationManager = AuthenticationManager.getInstance();
        reservationManager = getInstance(vertx);
        facilityManager = FacilityManager.getInstance();
        statisticsManager = StatisticsManager.getInstance();
    }

    private void registerListeners() {
        reservationManager.registerListener(new Listener() {
            @Override
            public void onReservationExpired(int reservationId) {
                reservationManager.getReservation(reservationId, ar -> {
                    if (ar.succeeded()) {
                        final JsonObject reservationObject = ar.result();
                        final String controllerPhysicalId = reservationObject.get("controller_physical_id").asString();
                        notifyControllerUpdated(controllerPhysicalId);
                        notifyReservationExpired(reservationId);
                    }
                });
            }

            @Override
            public void onTransactionStarted(int reservationId) {
                reservationManager.getReservation(reservationId, ar -> {
                    if (ar.succeeded()) {
                        final JsonObject reservationObject = ar.result();
                        notifyTransactionStarted(reservationId, reservationObject);
                    }
                });
            }

            @Override
            public void onTransactionEnded(int reservationId) {
                reservationManager.getReservation(reservationId, ar -> {
                    if (ar.succeeded()) {
                        final JsonObject reservationObject = ar.result();
                        notifyTransactionEnded(reservationId, reservationObject);
                    }
                });
            }
        });
    }

    private void listenChannels() {
        final INetworkConnection networkConnection = communicationProxy.getNetworkConnection();

        new SignUpResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> signUp(message)).listen();
        new LoginResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> login(message)).listen();
        new ReservableFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservableFacilities(message)).listen();
        new ControllerStatusSubscribeChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateControllerStatus(uri, message)).listen();
        new SlotStatusSubscribeChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateSlotStatus(uri, message)).listen();
        new GetFacilitiesResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getFacilities(message)).listen();
        new GetSlotsResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getSlots(uri, message)).listen();
        new GetStatisticsResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getStatistics(message)).listen();
        new MakeReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> makeReservation(uri, message)).listen();
        new ConfirmReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> confirmEnter(uri, message)).listen();
        new ConfirmExitResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> confirmLeave(uri, message)).listen();
        new GetReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> getReservation(message)).listen();
        new CancelReservationResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> cancelReservation(uri, message)).listen();
        new UpdateFacilityResponseChannel(networkConnection).addObserver((networkChannel, uri, message) -> updateFacility(uri, message)).listen();
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
        final String sessionKey = MakeReservationRequestChannel.getSessionKey(message);

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

    private void updateFacility(Uri uri, NetworkMessage message) {
        final int facilityId = UpdateFacilityRequestChannel.getFacilityId(uri);
        final String name = UpdateFacilityRequestChannel.getFacilityName(message);
        final double fee = UpdateFacilityRequestChannel.getFee(message);
        final int feeUnit = UpdateFacilityRequestChannel.getFeeUnit(message);
        final int gracePeriod = UpdateFacilityRequestChannel.getGracePeriod(message);

        // TODO: check session_key
        facilityManager.updateFacility(facilityId, name, fee, feeUnit, gracePeriod, ar -> {
            if (ar.failed()) {
                communicationProxy.responseFail(message, ar.cause());
            } else {
                communicationProxy.responseSuccess(message);
            }
        });
    }

    private void updateControllerStatus(Uri uri, NetworkMessage message) {
        final String controllerPhysicalId = ControllerStatusPublishChannel.getControllerPhysicalId(uri);
        final boolean updated = ControllerStatusPublishChannel.isUpdated(message); if (updated) return;
        final boolean available = ControllerStatusPublishChannel.isAvailable(message);

        facilityManager.updateControllerAvailable(controllerPhysicalId, available, ar -> {
            if (ar.failed()) {
                logger.info("updateControllerStatus: failed");
            } else {
                logger.info("updateControllerStatus: success");
            }
        });
    }

    private void updateSlotStatus(Uri uri, NetworkMessage message) {
        final String controllerPhysicalId = SlotStatusPublishChannel.getControllerPhysicalId(uri);
        final int slotNumber = SlotStatusPublishChannel.getSlotNumber(uri);
        final boolean parked = SlotStatusPublishChannel.isParked(message);
        final int confirmationNumber = SlotStatusPublishChannel.getConfirmationNumber(message);

        facilityManager.getSlot(controllerPhysicalId, slotNumber, ar1 -> {
            if (ar1.failed()) {
                ar1.cause().printStackTrace();
            } else {
                final JsonObject slotObject = ar1.result();
                final int parkedSlotId = slotObject.get("id").asInt();
                facilityManager.updateSlotParked(parkedSlotId, parked, ar2 -> {
                    if (ar2.failed()) {
                        ar2.cause().printStackTrace();
                    } else {
                        logger.info("updateSlotStatus: slot=" + slotObject + " updated parked=" + parked);
                        reservationManager.getReservationByConfirmationNumber(confirmationNumber, ar3 -> {
                            if (ar3.failed()) {
                                ar3.cause().printStackTrace();
                            } else {
                                final JsonObject reservationObject = ar3.result();
                                final int reservationId = reservationObject.get("id").asInt();
                                final int reservedSlotId = reservationObject.get("slot_id").asInt();
                                if (parkedSlotId != reservedSlotId) {
                                    logger.info("updateSlotStatus: parked incorrectly, perform slot reallocation now");
                                    reservationManager.reallocateSlot(reservationId, parkedSlotId, reservedSlotId, ar -> {
                                        if (ar.failed()) {
                                            ar.cause().printStackTrace();
                                        }
                                        notifyControllerUpdated(controllerPhysicalId);
                                    });
                                } else {
                                    logger.info("updateSlotStatus: parked correctly, reservation=" + reservationObject);
                                    notifyControllerUpdated(controllerPhysicalId);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void getFacilities(NetworkMessage message) {
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

    private void getStatistics(NetworkMessage message) {
        final String sessionKey = GetStatisticsRequestChannel.getSessionKey(message);
        final String query = GetStatisticsRequestChannel.getKeyDbqueryKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_OWNER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                statisticsManager.getStatisticsByQuery(query, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        final Statistics statistics = ar2.result();
                        final List<String> colunmnameList = statistics.getColunmnameList();
                        final List<JsonArray> valuesList = statistics.getValuesList();
                        communicationProxy.responseSuccess(message, GetStatisticsResponseChannel.createResponseObject(colunmnameList, valuesList));
                    }
                });
            }
        });
    }

    private void makeReservation(Uri uri, NetworkMessage message) {
        final String sessionKey = MakeReservationRequestChannel.getSessionKey(message);
        final int facilityId = MakeReservationRequestChannel.getFacilityId(uri);
        final int reservationTs = MakeReservationRequestChannel.getReservationTs(message);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userId = userObject.get("id").asInt();
                facilityManager.getReservableSlots(facilityId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        final List<JsonObject> slotObjects = ar2.result();
                        if (slotObjects.isEmpty()) {
                            communicationProxy.responseFail(message, "NO_AVAILABLE_SLOT");
                        } else {
                            final int slotId = slotObjects.get(0).get("id").asInt();
                            final int confirmationNumber = new Random().nextInt((9999 - 1000) + 1) + 1000;
                            facilityManager.getFacility(facilityId, ar3 -> {
                                if (ar3.failed()) {
                                    communicationProxy.responseFail(message, ar3.cause());
                                } else {
                                    final JsonObject facilityObject = ar3.result();
                                    final double fee = facilityObject.get("fee").asDouble();
                                    final int feeUnit = facilityObject.get("fee_unit").asInt();
                                    final int gracePeriod = facilityObject.get("grace_period").asInt();
                                    reservationManager.makeReservation(userId, slotId, reservationTs, confirmationNumber, fee, feeUnit, gracePeriod, ar4 -> {
                                        if (ar4.failed()) {
                                            communicationProxy.responseFail(message, ar4.cause());
                                        } else {
                                            final JsonObject reservationObject = ar4.result();
                                            final String controllerPhysicalId = reservationObject.get("controller_physical_id").asString();
                                            notifyControllerUpdated(controllerPhysicalId);
                                            communicationProxy.responseSuccess(message, ar4.result());
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

    private void confirmEnter(Uri uri, NetworkMessage message) {
        final int confirmationNumber = ConfirmReservationRequestChannel.getConfirmationNumber(message);
        final String controllerPhysicalId = ConfirmReservationRequestChannel.getControllerPhysicalId(uri);
        logger.info("confirmEnter: confirmationNumber=" + confirmationNumber + ", controllerPhysicalId=" + controllerPhysicalId);

        reservationManager.getReservationByConfirmationNumber(confirmationNumber, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject reservationObject = ar1.result();
                final int reservationId = reservationObject.get("id").asInt();
                final String reservationControllerPhysicalId = reservationObject.get("controller_physical_id").asString();
                final int reservationSlotNumber = reservationObject.get("slot_no").asInt();

                if (!controllerPhysicalId.equals(reservationControllerPhysicalId)) {
                    communicationProxy.responseFail(message, "WRONG_CONTROLLER");
                } else {
                    reservationManager.startTransaction(reservationId, ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            communicationProxy.responseSuccess(message, ConfirmReservationResponseChannel.createResponseObject(reservationSlotNumber));
                        }
                    });
                }
            }
        });
    }

    private void confirmLeave(Uri uri, NetworkMessage message) {
        final String controllerPhysicalId = ConfirmExitRequestChannel.getControllerPhysicalId(uri);
        final int slotNumber = ConfirmExitRequestChannel.getSlotNumber(message);
        logger.debug("confirmLeave: controllerPhysicalId=" + controllerPhysicalId + ", slotNumber=" + slotNumber);

        reservationManager.finalizeReservation(controllerPhysicalId, slotNumber, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                notifyControllerUpdated(controllerPhysicalId);
                communicationProxy.responseSuccess(message);
            }
        });
    }

    private void getReservation(NetworkMessage message) {
        final String sessionKey = GetReservationRequestChannel.getSessionKey(message);
        logger.debug("getReservation: sessionKey=" + sessionKey);

        authenticationManager.getSessionUser(sessionKey, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                final JsonObject userObject = ar1.result();
                final int userType = userObject.get("type").asInt();
                final int userId = userObject.get("id").asInt();
                if (userType != User.USER_TYPE_DRIVER) {
                    communicationProxy.responseFail(message, "NO_AUTHORIZATION");
                } else {
                    reservationManager.getReservationByUserId(userId, ar2 -> {
                        if (ar2.failed()) {
                            communicationProxy.responseFail(message, ar2.cause());
                        } else {
                            communicationProxy.responseSuccess(message, ar2.result());
                        }
                    });
                }
            }
        });
    }

    private void cancelReservation(Uri uri, NetworkMessage message) {
        final int reservationId = CancelReservationRequestChannel.getReservationId(uri);
        final String sessionKey = CancelReservationRequestChannel.getSessionKey(message);

        authenticationManager.checkUserType(sessionKey, User.USER_TYPE_DRIVER, ar1 -> {
            if (ar1.failed()) {
                communicationProxy.responseFail(message, ar1.cause());
            } else {
                reservationManager.getReservation(reservationId, ar2 -> {
                    if (ar2.failed()) {
                        communicationProxy.responseFail(message, ar2.cause());
                    } else {
                        final JsonObject reservationObject = ar2.result();
                        reservationManager.cancelReservation(reservationId, ar3 -> {
                            if (ar2.failed()) {
                                communicationProxy.responseFail(message, ar3.cause());
                            } else {
                                final String controllerPhysicalId = reservationObject.get("controller_physical_id").asString();
                                notifyControllerUpdated(controllerPhysicalId);
                                communicationProxy.responseSuccess(message);
                            }
                        });
                    }
                });
            }
        });
    }

    private void notifyReservationExpired(int reservationId) {
        new ReservationStatusPublishChannel(communicationProxy.getNetworkConnection(), reservationId).notify(ReservationStatusPublishChannel.createExpiredMessage());
    }

    private void notifyTransactionStarted(int reservationId, JsonObject reservationObject) {
        new ReservationStatusPublishChannel(communicationProxy.getNetworkConnection(), reservationId).notify(ReservationStatusPublishChannel.createTransactionStartedMessage(reservationObject));
    }

    private void notifyTransactionEnded(int reservationId, JsonObject reservationObject) {
        new ReservationStatusPublishChannel(communicationProxy.getNetworkConnection(), reservationId).notify(ReservationStatusPublishChannel.createTransactionEndedMessage(reservationObject));
    }

    private void notifyControllerUpdated(String controllerPhysicalId) {
        new ControllerStatusPublishChannel(communicationProxy.getNetworkConnection(), controllerPhysicalId).notify(ControllerStatusPublishChannel.createUpdatedMessage(true));
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