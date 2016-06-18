package com.lge.notyet.server.verticle;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.channels.LoginResponseChannel;
import com.lge.notyet.channels.SignUpRequestChannel;
import com.lge.notyet.channels.SignUpResponseChannel;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.server.manager.AuthenticationManager;
import com.lge.notyet.server.manager.ReservationManager;
import com.lge.notyet.server.manager.FacilityManager;
import com.lge.notyet.server.manager.StatisticsManager;
import com.lge.notyet.server.model.User;
import com.lge.notyet.server.proxy.CommunicationProxy;
import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.*;

public class MainVerticle extends AbstractVerticle {
    private static final String BROKER_HOST = "localhost";
    private static final String DB_HOST = "localhost";
    private static final String DB_USERNAME = "dba";
    private static final String DB_PASSWORD = "dba";

    private AuthenticationManager authenticationManager;
    private ReservationManager reservationManager;
    private FacilityManager facilityManager;
    private StatisticsManager statisticsManager;

    private DatabaseProxy databaseProxy;
    private CommunicationProxy communicationProxy;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
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