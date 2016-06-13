package com.lge.notyet.lib.comm;

public class WrapNetworkChannel extends NetworkChannel {

    NetworkChannel mHookedChannel = null;

    protected WrapNetworkChannel(NetworkChannel networkChannel) {
        super(networkChannel.getNetworkConnection());
        mHookedChannel = networkChannel;
    }

    @Override
    public Uri getChannelDescription() {
        return mHookedChannel.getChannelDescription();
    }

    @Override
    public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onNotify(networkChannel, uri, message);
    }

    @Override
    public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onRequest(networkChannel, uri, message);
    }

    @Override
    public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onResponse(networkChannel, uri, message);
    }

    @Override
    public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onTimeout(networkChannel, message);
    }
}
