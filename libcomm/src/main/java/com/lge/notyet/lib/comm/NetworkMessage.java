package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-10.
 */

import com.eclipsesource.json.JsonObject;

public abstract class NetworkMessage {

    public static final int MESSAGE_TYPE_UNKNOWN = -1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    static final String MSG_TYPE = "_msg_type_"; // Reserved

    private JsonObject mMessage = null;
    private int mMessageType = MESSAGE_TYPE_UNKNOWN;

    NetworkMessage(int messageType, JsonObject message) {
        mMessageType = messageType;
        mMessage = message;
    }

    public boolean isRequest() {
        return mMessageType == MESSAGE_TYPE_REQUEST;
    }
    public JsonObject getMessage() {
        return mMessage;
    }

    abstract protected void response_impl(JsonObject message) throws ExceptionInInitializerError;
    public void response(JsonObject message) throws ExceptionInInitializerError {
        response_impl(message);
    }
}
