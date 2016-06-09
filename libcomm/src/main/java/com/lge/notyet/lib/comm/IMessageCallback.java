package com.lge.notyet.lib.comm;

import io.vertx.core.json.JsonObject;

/**
 * Created by beney.kim on 2016-06-09.
 */
public interface IMessageCallback {
    void onMessage(String topic, String msg/*JsonObject notification*/);
}
