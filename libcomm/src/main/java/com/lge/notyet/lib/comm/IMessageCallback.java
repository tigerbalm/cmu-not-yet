package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */

public interface IMessageCallback {
    void onMessage(String topic, NetworkMessage msg);
}
