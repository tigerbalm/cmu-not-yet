package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-10.
 */

import com.eclipsesource.json.JsonObject;

public abstract class NetworkMessage <T> {

    abstract public byte[] getBytes();
    abstract protected void response_impl(NetworkMessage message) throws ExceptionInInitializerError;
    public void response(NetworkMessage message) throws ExceptionInInitializerError {
        response_impl(message);
    }

    abstract public T getMessage();
}
