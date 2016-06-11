package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 */

public interface IMessageTimeoutCallback {
    void onMessageTimeout(Uri requestedUri);
}
