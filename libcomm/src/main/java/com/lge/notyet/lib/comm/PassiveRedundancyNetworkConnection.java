package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Passive Redundancy for BaseConnection
 */

import com.eclipsesource.json.JsonObject;

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

    abstract public void subscribeSelfConfigurationChannel(Uri channelUri);
    abstract public void unsubscribeSelfConfigurationChannel(Uri channelUri);

    protected boolean preHandleConnected() {
        subscribeSelfConfigurationChannel(mConnectionUri);
        doSelfConfiguration();
        return false;
    }

    protected boolean preHandleLost() {
        log("S3 E. Oops ConnectionLost, mServerId=" + mServerId);
        mIsMaster = false;
        // unsubscribeSelfConfigurationChannel(mConnectionUri);
        mState = MASTER_SELF_CONFIGURATION_STATE_SLAVE;
        if (mContentionWindowDelayTask != null) mContentionWindowDelayTask.cancel(true);
        doSelfConfiguration();
        return false;
    }

    protected PassiveRedundancyNetworkConnection(String connectionName, BaseNetworkConnection networkConnection) {

        super(connectionName, networkConnection);

        mIsMaster = false;
        LOG_TAG = "PRNC-" + mServerId;

        log("Created, mServerId=" + mServerId);
        if (isConnected()) preHandleConnected();
    }

    public void disconnect() {
        log("S3 E. I am dying, mServerId=" + mServerId);
        mIsMaster = false;
        unsubscribeSelfConfigurationChannel(mConnectionUri);
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
                send(mConnectionUri, new JsonObject()
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_RESPONSE));
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
                send(mConnectionUri, new JsonObject()
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_REQUEST));

                mDuplicateDetectionWindowDelayTask = mScheduler.schedule(new DuplicateDetectionWindowDelayTask(),
                        MASTER_SELF_CONFIGURATION_DDW + mRandom.nextInt(MASTER_SELF_CONFIGURATION_DDW_RANDOM), MILLISECONDS);
            } else {
                // TODO: It should be not.
                log("ContentionWindowDelayTask - It should not be reached");
            }
        }
    }
    private ScheduledFuture<?> mContentionWindowDelayTask = null;

    protected boolean preHandleMessage(Uri uri, NetworkMessage msg) {

        if (uri != null && uri.getPath().equals(mConnectionUri.getPath())) {

            long rSrvId = 0L;
            String rMsgType = "";

            try {
                rSrvId = msg.getMessage().get(MASTER_SELF_CONFIGURATION_MESSAGE_ID).asLong();
                rMsgType = msg.getMessage().get(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE).asString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rSrvId == mServerId) return true;

            log("Received self-configuration message, type=" + rMsgType + " from server " + rSrvId + ", current state=" + mState);

            boolean isRequestMessage = MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_REQUEST.equals(rMsgType);

            if (isRequestMessage) {
                switch (mState) {
                    case MASTER_SELF_CONFIGURATION_STATE_SLAVE:
                        mContentionWindowDelayTask.cancel(true);
                        break;
                    case MASTER_SELF_CONFIGURATION_STATE_MASTER_CANDIDATE:
                        break;
                    case MASTER_SELF_CONFIGURATION_STATE_MASTER:
                        send(mConnectionUri, new JsonObject()
                                .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                                .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_RESPONSE));
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
            return true;
        }
        return false;
    }

    protected synchronized void doSelfConfiguration() {

        if (isConnected() == false) return;
        if (mContentionWindowDelayTask != null && mContentionWindowDelayTask.isDone() == false) return;

        log("S1 S. Contention Window Random Delay Started, state=" + mState);

        if (mState != MASTER_SELF_CONFIGURATION_STATE_SLAVE) return;

        mContentionWindowDelayTask = mScheduler.schedule(new ContentionWindowDelayTask(),
                mRandom.nextInt(MASTER_SELF_CONFIGURATION_CONTENTION_WINDOW), MILLISECONDS);
    }
}
