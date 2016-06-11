package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Active Redundancy for BaseConnection
 */

import com.eclipsesource.json.JsonObject;

import java.net.InetAddress;
import java.util.Random;

class ActiveRedundancyNetworkConnection implements INetworkConnection {

    String LOG_TAG;

    protected boolean mIsMaster = false;
    protected Uri mConnectionUri = null;

    protected BaseNetworkConnection mBaseNetworkConnection = null;

    static Random mRandom = null;
    protected long mServerId = 0L;

    private IMessageCallback mOriginalMessageCallback = null;
    boolean preHandleMessage(Uri topic, NetworkMessage msg) {
        return false;
    }
    protected boolean postHandleMessage(Uri uri, NetworkMessage msg) {
        return false;
    }

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

    ActiveRedundancyNetworkConnection(String connectionName, BaseNetworkConnection networkConnection) {

        mIsMaster = true;

        mRandom = new Random();
        mRandom.setSeed(System.currentTimeMillis());

        mServerId = Math.abs(mRandom.nextLong());
        mConnectionUri = new Uri("/_master_slave_/" + connectionName );
        mBaseNetworkConnection = networkConnection;

        LOG_TAG = "ARNC-" + mServerId;

        IMessageCallback intermediateMsgCallback = (uri, msg) -> {

            if (preHandleMessage(uri, msg)) return;
            if (mIsMaster && mOriginalMessageCallback != null) mOriginalMessageCallback.onMessage(uri, msg);
            postHandleMessage(uri, msg);
        };

        INetworkCallback netCb = networkConnection.getNetworkCallback();
        if (netCb != null) {
            mOriginalNetworkCallback = networkConnection.hookNetworkCallback(mNetworkCallback);
        }

        mOriginalMessageCallback = networkConnection.hookMessageCallback(intermediateMsgCallback);
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

    public void disconnect() {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.disconnect();
    }

    public boolean isConnected() {
        return mBaseNetworkConnection != null && mBaseNetworkConnection.isConnected();
    }

    public void subscribe(Uri uri) {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.subscribe(uri);
    }

    public void unsubscribe(Uri uri) {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.unsubscribe(uri);
    }

    public void send(Uri uri, JsonObject message) {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.send(uri, message);
    }

    public void request(Uri uri, JsonObject message, IMessageCallback responseCb) {
        request(uri, message, responseCb, null);
    }

    public void request(Uri uri, JsonObject message, IMessageCallback responseCb, IMessageTimeoutCallback timeoutCallback) {
        if (mBaseNetworkConnection != null) mBaseNetworkConnection.request(uri, message, responseCb, timeoutCallback);
    }

    void log (String log) {
        com.lge.notyet.lib.comm.util.Log.logd(LOG_TAG, log);
    }
    void logv(String log) {
        com.lge.notyet.lib.comm.util.Log.logv(LOG_TAG, log);
    }
}
