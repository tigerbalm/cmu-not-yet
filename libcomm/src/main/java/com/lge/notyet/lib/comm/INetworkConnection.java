package com.lge.notyet.lib.comm;

import java.net.InetAddress;

abstract public class INetworkConnection {

    abstract public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException;
    abstract public void disconnect();
    abstract public boolean isConnected();

    abstract protected void subscribe(NetworkChannel networkChannel);
    abstract protected void unsubscribe(NetworkChannel networkChannel);

    abstract protected void send(NetworkChannel networkChannel, NetworkMessage message);
    abstract protected void request(NetworkChannel networkChannel, NetworkMessage message);
}
