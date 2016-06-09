package com.lge.notyet.lib.comm;

import com.eclipsesource.json.JsonObject;

import java.net.InetAddress;

/**
 * Created by beney.kim on 2016-06-09.
 */
public interface INetworkChannel {

    void connect(InetAddress ipAddress);
    void subscribe(Uri uri);
    void send(Uri uri, JsonObject message);
    void request(Uri uri, JsonObject message, IMessageCallback responseCb);
}
