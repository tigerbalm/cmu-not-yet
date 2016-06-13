package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-13.
 * This class is used for RESERVED constants for this library
 */

final class MqttConstants {

    static final int DEFAULT_QOS = 2;

    static final String MSG_TYPE_KEY = "_msg_type_";

    static final String REQUEST_MESSAGE_TOPIC = "/request/";
    static final String RESPONSE_MESSAGE_TOPIC = "/response/";

    static final String WILL_MESSAGE_TOPIC = "/will";
    static final int WILL_MESSAGE_QOS = 2;
}
