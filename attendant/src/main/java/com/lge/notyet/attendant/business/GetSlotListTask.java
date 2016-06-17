package com.lge.notyet.attendant.business;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.ui.ITaskDoneCallback;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.GetSlotsRequestChannel;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GetSlotListTask implements Callable<Void> {

    private static final String LOG_TAG = "GetSlotListTask";

    private final String mSessionKey;
    private final int mFacilityId;
    private final ITaskDoneCallback mTaskDoneCallback;

    private GetSlotListTask(String sessionKey, int facilityId, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mFacilityId = facilityId;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        GetSlotsRequestChannel sc = ncm.createGetSlotsRequestChannel(mFacilityId);
        sc.addObserver(mGetSlotsResult);
        sc.addTimeoutObserver(mGetSlotsTimeout);

        boolean ret = sc.request(GetSlotsRequestChannel.createRequestMessage(mSessionKey));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mGetSlotsResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logd(LOG_TAG, "mGetSlotsResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mGetSlotsTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, int facilityId, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new GetSlotListTask(sessionKey, facilityId, taskDoneCallback));
    }
}
