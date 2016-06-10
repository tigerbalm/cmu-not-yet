package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.eclipsesource.json.JsonObject;
import java.net.InetAddress;

public interface INetworkChannel {
    void connect(InetAddress ipAddress, INetworkCallback networkCb);
    void disconnect();
    boolean isConnected();
    void subscribe(Uri uri);
    void unsubscribe(Uri uri);
    void send(Uri uri, JsonObject message);
    void request(Uri uri, JsonObject message, IMessageCallback responseCb);
}
