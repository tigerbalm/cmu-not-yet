package com.lge.notyet.kiosk.comm;

import com.lge.notyet.kiosk.message.MyMessage;

/**
 * Created by sjun.lee on 2016-06-21.
 */
public interface CommApi {
    boolean connect();
    boolean disconnect();
    int send(String string);

    boolean connected();
}
