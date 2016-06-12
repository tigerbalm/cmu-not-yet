package com.lge.notyet.lib.comm;

import java.util.ArrayList;

abstract public class ClientChannelRegistry extends ClientChannel {

    protected ClientChannelRegistry(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    ArrayList<IOnResponse> mIOnResponseList = new ArrayList <IOnResponse>();

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

    ArrayList<IOnTimeout> mIOnTimeoutList = new ArrayList <IOnTimeout>();

    public void addTimeoutObserver(IOnTimeout observer) {
        synchronized (ClientChannelRegistry.class) {
            mIOnTimeoutList.add(observer);
        }
    }

    public void removeTimeObserver(IOnTimeout observer) {
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
