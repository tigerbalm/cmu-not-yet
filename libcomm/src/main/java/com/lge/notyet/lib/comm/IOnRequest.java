package com.lge.notyet.lib.comm;

public interface IOnRequest {
    void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
}
