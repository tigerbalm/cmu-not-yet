package com.lge.notyet.lib.comm;

public interface IOnTimeout {
    void onTimeout(NetworkChannel networkChannel, NetworkMessage message);
}
