package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-10.
 */

import com.eclipsesource.json.JsonObject;

public abstract class NetworkMessage <T> {

    public static final int MESSAGE_TYPE_UNKNOWN = -1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    private int mMessageType = MESSAGE_TYPE_UNKNOWN;

    protected NetworkMessage(int messageType) {
        mMessageType = messageType;
    }

    public boolean isRequest() {
        return mMessageType == MESSAGE_TYPE_REQUEST;
    }

    abstract public byte[] getBytes();
    abstract protected void response_impl(NetworkMessage message) throws ExceptionInInitializerError;
    public void response(NetworkMessage message) throws ExceptionInInitializerError {
        response_impl(message);
    }

    abstract public T getMessage();
}
