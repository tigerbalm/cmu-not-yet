package com.lge.notyet.lib.comm;

import java.util.ArrayList;

abstract public class ServerChannelRegistry extends ServerChannel {

    protected ServerChannelRegistry(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    ArrayList<IOnRequest> mIOnRequestList = new ArrayList <IOnRequest>();

    public void addObserver(IOnRequest observer) {
        synchronized (ServerChannelRegistry.class) {
            mIOnRequestList.add(observer);
        }
    }

    public void removeObserver(IOnRequest observer) {
        synchronized (ServerChannelRegistry.class) {
            mIOnRequestList.remove(observer);
        }
    }

    @Override
    public final void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

        synchronized (ServerChannelRegistry.class) {
            for (IOnRequest observer : mIOnRequestList) {
                observer.onRequest(networkChannel, uri, message);
            }
        }
    }
}
