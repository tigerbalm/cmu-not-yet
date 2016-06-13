package com.lge.notyet.lib.comm;

public interface INetworkCallback {
    void onConnected();
    void onConnectFailed();
    void onLost();
}
