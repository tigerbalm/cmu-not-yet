package com.lge.notyet.lib.comm;

import java.util.ArrayList;

abstract public class ClientChannelRegistry extends ClientChannel {

    protected ClientChannelRegistry(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    private final ArrayList<IOnResponse> mIOnResponseList = new ArrayList <>();

    public void addObserver(IOnResponse observer) {
        synchronized (ClientChannelRegistry.class) {
            mIOnResponseList.add(observer);
        }
    }

    public void removeObserver(IOnResponse observer) {
        synchronized (ClientChannelRegistry.class) {
            mIOnResponseList.remove(observer);
        }
    }

    @Override
    public final void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

        synchronized (ClientChannelRegistry.class) {
            for (IOnResponse observer : mIOnResponseList) {
                observer.onResponse(networkChannel, uri, message);
            }
        }
    }

    private final ArrayList<IOnTimeout> mIOnTimeoutList = new ArrayList <>();

    public void addTimeoutObserver(IOnTimeout observer) {
        synchronized (ClientChannelRegistry.class) {
            mIOnTimeoutList.add(observer);
        }
    }

    public void removeTimeoutObserver(IOnTimeout observer) {
        synchronized (ClientChannelRegistry.class) {
            mIOnTimeoutList.remove(observer);
        }
    }

    @Override
    public final void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {

        synchronized (ClientChannelRegistry.class) {
            for (IOnTimeout observer : mIOnTimeoutList) {
                observer.onTimeout(networkChannel, message);
            }
        }
    }
}
