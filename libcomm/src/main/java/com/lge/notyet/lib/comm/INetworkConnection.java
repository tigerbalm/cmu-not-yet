package com.lge.notyet.lib.comm;

import java.net.InetAddress;

abstract public class INetworkConnection {

    abstract public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException;
    abstract public void disconnect();
    abstract public boolean isConnected();

    /* I want to hide this functions from applications, because channel is our unique interface in this project. */
    abstract protected void subscribe(NetworkChannel networkChannel);
    abstract protected void unsubscribe(NetworkChannel networkChannel);
    abstract protected boolean send(NetworkChannel networkChannel, NetworkMessage message);
    abstract protected boolean request(NetworkChannel networkChannel, NetworkMessage message);
}
