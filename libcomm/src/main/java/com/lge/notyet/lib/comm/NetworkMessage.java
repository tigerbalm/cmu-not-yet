package com.lge.notyet.lib.comm;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Created by beney.kim on 2016-06-10.
 */
public abstract class NetworkMessage {

    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    static final String MSG_TYPE = "_msg_type_";

    private JsonObject mMessage;

    protected NetworkMessage(int messageType, JsonObject message) {

        JsonValue jtype = message.get(MSG_TYPE);

        if (jtype != null) {
            mMessage = message;

            /*
            int type = jtype.asInt();
            if (type != messageType) {
                mMessage.set(MSG_TYPE, messageType);
            }
            */
        } else {

            mMessage = message;
            mMessage.add(MSG_TYPE, messageType);
        }
    }

    public boolean isRequest() {
        return mMessage.get(MSG_TYPE).asInt() == MESSAGE_TYPE_REQUEST;
    }


    public JsonObject getMessage() {
        return mMessage;
    }

    abstract protected void response_impl(JsonObject message);

    public void response(JsonObject message) {
        response_impl(message);
    }
}
