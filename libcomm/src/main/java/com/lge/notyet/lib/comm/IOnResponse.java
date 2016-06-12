package com.lge.notyet.lib.comm;

public interface IOnResponse {
    void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
}
