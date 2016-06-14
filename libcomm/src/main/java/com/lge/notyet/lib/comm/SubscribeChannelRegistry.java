package com.lge.notyet.lib.comm;

import java.util.ArrayList;

abstract public class SubscribeChannelRegistry extends SubscribeChannel {

    protected SubscribeChannelRegistry(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    ArrayList<IOnNotify> mIOnNotifyList = new ArrayList <IOnNotify>();

    public SubscribeChannelRegistry addObserver(IOnNotify observer) {
        synchronized (SubscribeChannelRegistry.class) {
            mIOnNotifyList.add(observer);
        }
        return this;
    }

    public SubscribeChannelRegistry removeObserver(IOnNotify observer) {
        synchronized (SubscribeChannelRegistry.class) {
            mIOnNotifyList.remove(observer);
        }
        return this;
    }

    @Override
    public final void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

        synchronized (SubscribeChannelRegistry.class) {
            for (IOnNotify observer : mIOnNotifyList) {
                observer.onNotify(networkChannel, uri, message);
            }
        }
    }
}
