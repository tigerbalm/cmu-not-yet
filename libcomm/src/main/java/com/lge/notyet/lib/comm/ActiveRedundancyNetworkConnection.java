package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Active Redundancy for BaseConnection
 */

import java.net.InetAddress;
import java.util.Random;

class ActiveRedundancyNetworkConnection extends INetworkConnection {

    String LOG_TAG;

    protected boolean mIsMaster = false;

    protected BaseNetworkConnection mBaseNetworkConnection = null;

    static Random mRandom = null;
    protected long mServerId = 0L;

    protected INetworkCallback mOriginalNetworkCallback = null;
    protected boolean preHandleConnected() {
        return false;
    }
    protected boolean postHandleConnected() {
        return false;
    }
    protected boolean preHandleConnectFailed() {
        return false;
    }
    protected boolean postHandleConnectFailed() {
        return false;
    }
    protected boolean preHandleLost() {
        return false;
    }
    protected boolean postHandleLost() {
        return false;
    }

    ActiveRedundancyNetworkConnection(BaseNetworkConnection networkConnection) {

        mIsMaster = true;

        mRandom = new Random();
        mRandom.setSeed(System.currentTimeMillis());

        mServerId = Math.abs(mRandom.nextLong());
        mBaseNetworkConnection = networkConnection;

        LOG_TAG = "ARNC-" + mServerId;

        INetworkCallback netCb = networkConnection.getNetworkCallback();
        if (netCb != null) {
            mOriginalNetworkCallback = networkConnection.hookNetworkCallback(mNetworkCallback);
        }
    }

    protected final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            if (preHandleConnected()) return;
            if (mOriginalNetworkCallback != null) mOriginalNetworkCallback.onConnected();
            postHandleConnected();
        }

        @Override
        public void onConnectFailed() {
            if (preHandleConnectFailed()) return;
            if (mOriginalNetworkCallback != null) mOriginalNetworkCallback.onConnectFailed();
            postHandleConnectFailed();
        }

        @Override
        public void onLost() {
            if (preHandleLost()) return;
            if (mOriginalNetworkCallback != null) mOriginalNetworkCallback.onLost();
            postHandleLost();
        }
    };

    public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException {

        if (mBaseNetworkConnection != null) {
            mOriginalNetworkCallback = networkCb;
            mBaseNetworkConnection.connect(ipAddress, mNetworkCallback);
        }
    }

    @Override
    public void disconnect() {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return mBaseNetworkConnection != null && mBaseNetworkConnection.isConnected();
    }

    @Override
    protected void subscribe(NetworkChannel networkChannel) {

        if (mBaseNetworkConnection != null) mBaseNetworkConnection.subscribe(networkChannel);
    }

    @Override
    protected void unsubscribe(NetworkChannel networkChannel) {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.unsubscribe(networkChannel);
    }

    @Override
    protected boolean send(NetworkChannel networkChannel, NetworkMessage message) {
        return mBaseNetworkConnection != null && mBaseNetworkConnection.send(networkChannel, message);
    }

    @Override
    protected boolean request(NetworkChannel networkChannel, NetworkMessage message) {
        return mBaseNetworkConnection != null && mBaseNetworkConnection.request(networkChannel, message);
    }

    void log (String log) {
        com.lge.notyet.lib.comm.util.Log.logd(LOG_TAG, log);
    }
}
