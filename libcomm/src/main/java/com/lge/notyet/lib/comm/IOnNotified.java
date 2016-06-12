package com.lge.notyet.lib.comm;


public interface IOnNotified {
    void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
}
