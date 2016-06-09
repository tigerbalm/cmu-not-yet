package com.lge.notyet.lib.comm;

import java.net.InetAddress;

/**
 * Created by beney.kim on 2016-06-09.
 */
public interface INetworkChannel {

    void connect(InetAddress ipAddress);
    void subscribe(Uri uri);
    void send(Uri uri, String msg/* JsonObject obj*/);
    void request(Uri uri, String msg/* JsonObject obj*/, IMessageCallback responseCb);
}
