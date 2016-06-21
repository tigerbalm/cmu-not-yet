package com.lge.notyet.attendant.business;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.GetFacilitiesRequestChannel;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class GetFacilityTask implements Callable<Void> {

    private static final String LOG_TAG = "GetFacilityTask";

    private final String mSessionKey;
    private final ITaskDoneCallback mTaskDoneCallback;

    private GetFacilityTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        GetFacilitiesRequestChannel fc = ncm.createGetFacilitiesRequestChannel();
        fc.addObserver(mGetFacilityResult);
        fc.addTimeoutObserver(mGetFacilityTimeout);

        boolean ret = fc.request(GetFacilitiesRequestChannel.createRequestMessage(mSessionKey));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    private final IOnResponse mGetFacilityResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mGetFacilityResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mGetFacilityTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new GetFacilityTask(sessionKey, taskDoneCallback));
    }

}
