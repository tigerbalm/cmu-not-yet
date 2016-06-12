package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

public class WrapNetworkChannel extends NetworkChannel {

    protected NetworkChannel mHookedChannel = null;

    protected WrapNetworkChannel(NetworkChannel networkChannel) {
        super(networkChannel.getNetworkConnection());
        mHookedChannel = networkChannel;
    }

    @Override
    public Uri getChannelDescription() {
        return mHookedChannel.getChannelDescription();
    }

    @Override
    public void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onNotified(networkChannel, uri, message);
    }

    @Override
    public void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        if (mHookedChannel != null) mHookedChannel.onRequested(networkChannel, uri, message);
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
