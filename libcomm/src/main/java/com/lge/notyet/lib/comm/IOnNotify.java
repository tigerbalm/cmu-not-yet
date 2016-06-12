package com.lge.notyet.lib.comm;

public interface IOnNotify {
    void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
}
