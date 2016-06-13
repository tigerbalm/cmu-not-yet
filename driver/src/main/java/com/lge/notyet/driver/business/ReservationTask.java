package com.lge.notyet.driver.business;

import com.lge.notyet.channels.ReservationMessage;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class ReservationTask implements Callable<Void> {

    private int mFacilityId;

    public ReservationTask(int facilityId) {
        mFacilityId = facilityId;
    }

    @Override
    public Void call() throws Exception {

        System.out.println("Start Task!!");

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        ReservationRequestChannel rc = ncm.createReservationChannel(mFacilityId);
        rc.addObserver(mReservationResult);
        rc.addTimeoutObserver(mReservationTimeout);
        System.out.println("Start Task2222222!!");
        ReservationMessage message = new ReservationMessage().setUserId("beney").setDate("6/1").setDate("11").setCreditCardNumber("1111-2222-3333-4444");
        rc.request(message);

        Thread.sleep(1000);
        return null;
    }


    // Business Logic here, we have no time :(
    IOnResponse mReservationResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("Reservation Result=" + message);
        }
    };

    IOnTimeout mReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
        }
    };

    public static FutureTask<Void> getTask(int facilityId) {
        return new FutureTask<Void>(new ReservationTask(facilityId));
    }
}
