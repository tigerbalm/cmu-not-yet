package com.lge.notyet.lib.comm;

public interface IOnRequested {
    void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
}
