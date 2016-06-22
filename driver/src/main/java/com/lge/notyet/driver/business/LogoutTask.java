package com.lge.notyet.driver.business;

import com.lge.notyet.channels.LogoutRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class LogoutTask implements Callable<Void> {

    private static final String LOG_TAG = "LoginTask";

    private final String mSessionKey;
    private final ITaskDoneCallback mTaskDoneCallback;

    private LogoutTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        LogoutRequestChannel logoutRequestChannel = ncm.createLogoutChannel();
        logoutRequestChannel.addObserver(mLogoutResult);
        logoutRequestChannel.addTimeoutObserver(mLogoutTimeout);

        boolean ret = logoutRequestChannel.request(LogoutRequestChannel.createRequestMessage(mSessionKey));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }

        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mLogoutResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mLogoutResult, result=" + message.getMessage());
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mLogoutTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "mLoginTimeout, Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new LogoutTask(sessionKey, taskDoneCallback));
    }
}
