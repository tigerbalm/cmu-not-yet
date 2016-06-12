package com.lge.notyet.lib.comm;

public abstract class NetworkMessage <T> {

    abstract protected void response_impl(NetworkMessage message) throws ExceptionInInitializerError;
    public void response(NetworkMessage message) throws ExceptionInInitializerError {
        response_impl(message);
    }

    abstract public T getMessage();
}
