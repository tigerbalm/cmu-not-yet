package com.lge.notyet.lib.comm;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Created by beney.kim on 2016-06-10.
 */
public abstract class NetworkMessage {

    public static final int MESSAGE_TYPE_UNKNOWN = -1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    static final String MSG_TYPE = "_msg_type"; // Reserved

    private JsonObject mMessage = null;
    private int mMessageType = MESSAGE_TYPE_UNKNOWN;

    protected NetworkMessage(int messageType, JsonObject message) {
        mMessageType = messageType;
        mMessage = message;
    }

    public boolean isRequest() {
        return mMessageType == MESSAGE_TYPE_REQUEST;
    }
    public JsonObject getMessage() {
        return mMessage;
    }

    abstract protected void response_impl(JsonObject message);
    public void response(JsonObject message) {
        response_impl(message);
    }
}
