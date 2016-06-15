package com.lge.notyet.owner.manager;

import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.lib.comm.INetworkCallback;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkConnection;

import java.net.InetAddress;

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
                mNc.connect(
                        //InetAddress.getLoopbackAddress(),
                        //InetAddress.getByName("192.168.1.20"),
                        InetAddress.getByName("192.168.1.21"),
                        //InetAddress.getByName("128.237.212.113"),
                        //InetAddress.getByName("128.237.206.5"),
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

    public LoginRequestChannel createLoginChannel() {
        return new LoginRequestChannel(mNc);
    }
}
