package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Active Redundancy for BaseChannel
 */

import com.eclipsesource.json.JsonObject;

import java.net.InetAddress;
import java.util.Random;

class ActiveRedundancyNetworkConnection implements INetworkConnection {

    String LOG_TAG;

    protected boolean mIsMaster = false;
    protected Uri mChannelUri = null;

    protected BaseNetworkConnection mBaseNetworkChannel = null;

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

    ActiveRedundancyNetworkConnection(String channelName, BaseNetworkConnection networkChannel) {

        mIsMaster = true;

        mRandom = new Random();
        mRandom.setSeed(System.currentTimeMillis());

        mServerId = Math.abs(mRandom.nextLong());
        mChannelUri = new Uri("/_master_slave_/" + channelName );
        mBaseNetworkChannel = networkChannel;

        LOG_TAG = "ARNC-" + mServerId;

        IMessageCallback intermediateMsgCallback = (uri, msg) -> {

            if (preHandleMessage(uri, msg)) return;
            if (mIsMaster && mOriginalMessageCallback != null) mOriginalMessageCallback.onMessage(uri, msg);
            postHandleMessage(uri, msg);
        };

        INetworkCallback netCb = networkChannel.getNetworkCallback();
        if (netCb != null) {
            mOriginalNetworkCallback = networkChannel.hookNetworkCallback(mNetworkCallback);
        }

        mOriginalMessageCallback = networkChannel.hookMessageCallback(intermediateMsgCallback);
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

        if (mBaseNetworkChannel != null) {
            mOriginalNetworkCallback = networkCb;
            mBaseNetworkChannel.connect(ipAddress, mNetworkCallback);
        }
    }

    public void disconnect() {
        if (mBaseNetworkChannel != null) mBaseNetworkChannel.disconnect();
    }

    public boolean isConnected() {
        return mBaseNetworkChannel != null && mBaseNetworkChannel.isConnected();
    }

    public void subscribe(Uri uri) {
        if (mBaseNetworkChannel != null) mBaseNetworkChannel.subscribe(uri);
    }

    public void unsubscribe(Uri uri) {
        if (mBaseNetworkChannel != null) mBaseNetworkChannel.unsubscribe(uri);
    }

    public void send(Uri uri, JsonObject message) {
        if (mBaseNetworkChannel != null) mBaseNetworkChannel.send(uri, message);
    }

    public void request(Uri uri, JsonObject message, IMessageCallback responseCb) {
        request(uri, message, responseCb, null);
    }

    public void request(Uri uri, JsonObject message, IMessageCallback responseCb, IMessageTimeoutCallback timeoutCallback) {
        if (mBaseNetworkChannel != null) mBaseNetworkChannel.request(uri, message, responseCb, timeoutCallback);
    }

    void log (String log) {
        com.lge.notyet.lib.comm.util.Log.logd(LOG_TAG, log);
    }
    void logv(String log) {
        com.lge.notyet.lib.comm.util.Log.logv(LOG_TAG, log);
    }
}
