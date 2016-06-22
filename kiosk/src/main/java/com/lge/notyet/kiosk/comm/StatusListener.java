package com.lge.notyet.kiosk.comm;

import com.lge.notyet.kiosk.message.MyMessage;

/**
 * Created by sjun.lee on 2016-06-21.
 */
public interface StatusListener {
    void onConnected();
    void onDisconnected();

    void onMessageReceived(String message);

    // topic##message
    void onSystemMessageReceived(String message);
}