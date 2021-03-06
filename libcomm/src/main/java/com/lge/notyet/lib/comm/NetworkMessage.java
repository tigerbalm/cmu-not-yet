package com.lge.notyet.lib.comm;

public abstract class NetworkMessage <T> {

    public static final int MESSAGE_TYPE_UNKNOWN = -1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    protected final T mMessage;

    public NetworkMessage(T message) {
        mMessage = message;
    }

    public T getMessage() {
        return mMessage;
    }

    public byte[] getBytes() {
        return mMessage.toString().getBytes();
    }

    abstract public void responseFor(NetworkMessage message);

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "clazz=" + this.getClass().getSimpleName() +
                ", message=" + mMessage +
                '}';
    }
}
