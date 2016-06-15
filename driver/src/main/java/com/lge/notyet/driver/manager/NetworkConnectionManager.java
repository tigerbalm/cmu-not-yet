package com.lge.notyet.driver.manager;

import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkCallback;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkConnectionManager {

    private INetworkConnection mNc = null;

    public static NetworkConnectionManager sNetworkConnectionManager = null;

    private NetworkConnectionManager () {
        mNc = new MqttNetworkConnection(null);
    }

    public static NetworkConnectionManager getInstance() {
        synchronized (NetworkConnectionManager.class) {
            if (sNetworkConnectionManager == null) sNetworkConnectionManager = new NetworkConnectionManager();
        }
        return sNetworkConnectionManager;
    }

    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            System.out.println("onConnected");
        }

        @Override
        public void onConnectFailed() {
        }

        @Override
        public void onLost() {
            // Reconnect
            //mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
        }
    };

    public void open() {
        if (!mNc.isConnected()) {
            try {
                mNc.connect(InetAddress.getLoopbackAddress(),
                        //InetAddress.getByName("128.237.212.113"),
                        //InetAddress.getByName("10.245.148.224"),
                        mNetworkCallback);
            } catch (/*UnknownHost*/Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (!mNc.isConnected()) {
            mNc.disconnect();
        }
    }

    public ReservationRequestChannel createReservationChannel(int facility) {
        return new ReservationRequestChannel(mNc, facility);
    }

    public LoginRequestChannel createLoginChannel() {
        return new LoginRequestChannel(mNc);
    }

    public GetReservationRequestChannel createGetReservationRequestChannel() {
        return new GetReservationRequestChannel(mNc);
    }

    public ReservableFacilitiesRequestChannel createReservableFacilitiesRequestChannel() {
        return new ReservableFacilitiesRequestChannel(mNc);
    }

    public SignUpRequestChannel createSignUpChannel() {
        return new SignUpRequestChannel(mNc);
    }

}
