package com.lge.notyet.attendant.manager;

import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.INetworkCallback;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkConnection;

import java.net.InetAddress;

public class NetworkConnectionManager {

    private static final String LOG_TAG = "NetworkConnectionManager";

    private INetworkConnection mNc = null;

    private static NetworkConnectionManager sNetworkConnectionManager = null;

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
            Log.logd(LOG_TAG, "onConnected");
        }

        @Override
        public void onConnectFailed() {
            Log.logd(LOG_TAG, "onConnectFailed");
        }

        @Override
        public void onLost() {
            Log.logd(LOG_TAG, "onLost");
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
                        //InetAddress.getByName("192.168.1.21"),
                        InetAddress.getByName("192.168.1.22"),
                        //InetAddress.getByName("128.237.175.140"),
                        //InetAddress.getByName("128.237.206.5"),
                        //InetAddress.getByName("10.245.148.224"),
                        mNetworkCallback);
            } catch (/*UnknownHost*/Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (mNc.isConnected()) {
            mNc.disconnect();
        }
    }

    public LoginRequestChannel createLoginChannel() {
        return new LoginRequestChannel(mNc);
    }

    public LogoutRequestChannel createLogoutChannel() {
        return new LogoutRequestChannel(mNc);
    }

    public GetFacilitiesRequestChannel createGetFacilitiesRequestChannel() {
        return new GetFacilitiesRequestChannel(mNc);
    }

    public GetSlotsRequestChannel createGetSlotsRequestChannel(int facilityId) {
        return new GetSlotsRequestChannel(mNc, facilityId);
    }

    public ControllerStatusSubscribeChannel createUpdateControllerStatusSubscribeChannel() {
        return new ControllerStatusSubscribeChannel(mNc);
    }

    public ControllerErrorReportSubscribeChannel createControllerErrorReportSubscribeChannel() {
        return new ControllerErrorReportSubscribeChannel(mNc);
    }

    public ConfirmExitRequestChannel createConfirmExitRequestChannel(String physicalId) {
        return new ConfirmExitRequestChannel(mNc, physicalId);
    }
}
