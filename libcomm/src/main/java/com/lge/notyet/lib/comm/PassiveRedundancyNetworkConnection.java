package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Passive Redundancy for BaseConnection
 */

import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.*;

public abstract class PassiveRedundancyNetworkConnection extends ActiveRedundancyNetworkConnection {

    private static final int MASTER_SELF_CONFIGURATION_CONTENTION_WINDOW = 5000;
    private static final int MASTER_SELF_CONFIGURATION_DDW = 3000;
    private static final int MASTER_SELF_CONFIGURATION_DDW_RANDOM = 2000;

    private static final int MASTER_SELF_CONFIGURATION_STATE_SLAVE = 0;
    private static final int MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE = 1;
    private static final int MASTER_SELF_CONFIGURATION_STATE_MASTER = 2;
    private int mState;

    protected static final String MASTER_SELF_CONFIGURATION_MESSAGE_ID = "server_id";
    protected static final String MASTER_SELF_CONFIGURATION_MESSAGE_TYPE = "type";
    protected static final String MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_REQUEST = "request";
    protected static final String MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_RESPONSE = "response";
    protected static final String MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_WILL = "will";

    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(3);

    private SelfConfigurationChannel mSelfConfigurationChannel = null;
    protected String mChannelName = null;

    protected boolean preHandleConnected() {
        mSelfConfigurationChannel.listen();
        doSelfConfiguration();
        return false;
    }

    protected boolean preHandleLost() {
        log("S3 E. Oops ConnectionLost, mServerId=" + mServerId);
        mIsMaster = false;

        mState = MASTER_SELF_CONFIGURATION_STATE_SLAVE;
        if (mContentionWindowDelayTask != null) mContentionWindowDelayTask.cancel(true);
        doSelfConfiguration();
        return false;
    }

    protected PassiveRedundancyNetworkConnection(String connectionName, BaseNetworkConnection networkConnection) {

        super(networkConnection);

        mIsMaster = false;
        LOG_TAG = "PRNC-" + mServerId;

        mChannelName = connectionName;

        log("Created, mServerId=" + mServerId);

        mSelfConfigurationChannel = new SelfConfigurationChannel(networkConnection);

        if (isConnected()) {
            preHandleConnected();
        }
    }

    public void disconnect() {
        log("S3 E. I am dying, mServerId=" + mServerId);
        mIsMaster = false;
        mSelfConfigurationChannel.unlisten();
        mState = MASTER_SELF_CONFIGURATION_STATE_SLAVE;
        if (mContentionWindowDelayTask != null) mContentionWindowDelayTask.cancel(true);
        super.disconnect();
    }

    private class DuplicateDetectionWindowDelayTask implements Runnable {

        public void run() {

            log("S2 E. Broadcast Probe is over without response, mServerId=" + mServerId);

            if (mState == MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE) {
                mState = MASTER_SELF_CONFIGURATION_STATE_MASTER;
                mIsMaster = true;
                mSelfConfigurationChannel.notify(getMasterAdvertisementMessage());
                log("S3 S. I am new Master Node, mServerId=" + mServerId);
            } else {
                // TODO: It should be not.
                log("DuplicateDetectionWindowDelayTask - It should not be reached, mState=" + mState);
            }
        }
    }
    private ScheduledFuture<?> mDuplicateDetectionWindowDelayTask = null;

    private class ContentionWindowDelayTask implements Runnable {

        public void run() {

            log("S1 E. Contention Window Random Delay is over");

            if (isConnected()) {

                log("S2 S. Broadcast Probe from mServerId=" + mServerId);
                mState = MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE;
                mSelfConfigurationChannel.notify(getMasterSolicitationMessage());

                mDuplicateDetectionWindowDelayTask = mScheduler.schedule(new DuplicateDetectionWindowDelayTask(),
                        MASTER_SELF_CONFIGURATION_DDW + mRandom.nextInt(MASTER_SELF_CONFIGURATION_DDW_RANDOM), MILLISECONDS);
            } else {
                // TODO: It should be not.
                log("ContentionWindowDelayTask - It should not be reached");
            }
        }
    }
    private ScheduledFuture<?> mContentionWindowDelayTask = null;

    public class PassiveRedundancyNetworkChannel extends WrapNetworkChannel {

        PassiveRedundancyNetworkChannel(NetworkChannel networkChannel) {
            super(networkChannel);
        }

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            if (mIsMaster) super.onNotify(networkChannel, uri, message);
        }

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            if (mIsMaster) super.onRequest(networkChannel, uri, message);
        }

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            NetworkChannel nc =  mPassiveRedundancyNetworkChannels.get(mHookedChannel.getHashKey());
            if (nc != null) {
                mPassiveRedundancyNetworkChannels.remove(mHookedChannel.getHashKey());
            }

            if (mIsMaster) super.onResponse(networkChannel, uri, message);
        }

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {

            NetworkChannel nc =  mPassiveRedundancyNetworkChannels.get(mHookedChannel.getHashKey());
            if (nc != null) {
                mPassiveRedundancyNetworkChannels.remove(mHookedChannel.getHashKey());
            }

            if (mIsMaster) super.onTimeout(networkChannel, message);
        }
    }

    private final ConcurrentHashMap<String, NetworkChannel> mPassiveRedundancyNetworkChannels = new ConcurrentHashMap<>();

    @Override
    protected void subscribe(NetworkChannel networkChannel) {
        NetworkChannel nc = new PassiveRedundancyNetworkChannel(networkChannel);
        mPassiveRedundancyNetworkChannels.put(networkChannel.getHashKey(), nc);
        super.subscribe(nc);
    }

    @Override
    protected void unsubscribe(NetworkChannel networkChannel) {
        NetworkChannel nc =  mPassiveRedundancyNetworkChannels.get(networkChannel.getHashKey());
        if (nc != null) {
            super.unsubscribe(new PassiveRedundancyNetworkChannel(nc));
            mPassiveRedundancyNetworkChannels.remove(networkChannel.getHashKey());
        }
    }

    @Override
    protected void request(NetworkChannel networkChannel, NetworkMessage message) {
        NetworkChannel nc = new PassiveRedundancyNetworkChannel(networkChannel);
        mPassiveRedundancyNetworkChannels.put(networkChannel.getHashKey(), nc);
        super.request(nc, message);
    }

    public class SelfConfigurationChannel extends NotificationChannel {

        SelfConfigurationChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return getSelfConfigurationUri();
        }

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            if (uri != null && uri.getPath().equals(getSelfConfigurationUri().getPath())) {

                boolean isSolicitationMessage = false;

                try {
                    isSolicitationMessage = isSolicitationMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isLoopbackMessage(message)) return;

                log("Received self-configuration message, solicitation=" + isSolicitationMessage + ", current state=" + mState);

                if (isSolicitationMessage) {
                    switch (mState) {
                        case MASTER_SELF_CONFIGURATION_STATE_SLAVE:
                            mContentionWindowDelayTask.cancel(true);
                            break;
                        case MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE:
                            break;
                        case MASTER_SELF_CONFIGURATION_STATE_MASTER:
                            notify(getMasterAdvertisementMessage());
                            break;
                        default:
                            log("preHandleMessage - It should not be reached");
                            break;
                    }
                }
                // It should be response message
                else {
                    switch (mState) {
                        case MASTER_SELF_CONFIGURATION_STATE_SLAVE:
                            mContentionWindowDelayTask.cancel(true);
                            break;
                        case MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE:
                            mDuplicateDetectionWindowDelayTask.cancel(true);
                            mState = MASTER_SELF_CONFIGURATION_STATE_SLAVE;
                            break;
                        default:
                            log("preHandleMessage - It should not be reached");
                            mState = MASTER_SELF_CONFIGURATION_STATE_SLAVE;
                            break;
                    }
                }
            }
        }
    }

    protected synchronized void doSelfConfiguration() {

        if (!isConnected()) return;
        if (mContentionWindowDelayTask != null && !mContentionWindowDelayTask.isDone()) return;

        log("S1 S. Contention Window Random Delay Started, state=" + mState);

        if (mState != MASTER_SELF_CONFIGURATION_STATE_SLAVE) return;

        mContentionWindowDelayTask = mScheduler.schedule(new ContentionWindowDelayTask(),
                mRandom.nextInt(MASTER_SELF_CONFIGURATION_CONTENTION_WINDOW), MILLISECONDS);
    }

    abstract protected Uri getSelfConfigurationUri();
    abstract protected NetworkMessage getMasterSolicitationMessage();
    abstract protected NetworkMessage getMasterAdvertisementMessage();

    abstract protected boolean isLoopbackMessage(NetworkMessage message);
    abstract protected boolean isSolicitationMessage(NetworkMessage message);

}
