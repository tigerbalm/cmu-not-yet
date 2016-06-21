package com.lge.notyet.attendant.business;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.ConfirmExitRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class RequestManualExitTask implements Callable<Void> {

    private static final String LOG_TAG = "GetFacilityTask";

    private final String mSessionKey;
    private final String mControllerPhysicalId;
    private final int mSlotNumber;
    private final ITaskDoneCallback mTaskDoneCallback;

    private RequestManualExitTask(String sessionKey, String controllerPhysicalId, int slotNumber, ITaskDoneCallback taskDoneCallback) {

        mSessionKey = sessionKey;
        mControllerPhysicalId = controllerPhysicalId;
        mSlotNumber = slotNumber;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        ConfirmExitRequestChannel fc = ncm.createConfirmExitRequestChannel(mControllerPhysicalId);
        fc.addObserver(mManualExitResult);
        fc.addTimeoutObserver(mManualExitTimeout);

        MqttNetworkMessage reqMsg = ConfirmExitRequestChannel.createRequestMessage(mSlotNumber);
        reqMsg.getMessage().add("session_key", mSessionKey);

        Log.logv(LOG_TAG, "request Manual Exit, message=" + reqMsg);
        boolean ret = fc.request(reqMsg);
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    private final IOnResponse mManualExitResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mManualExitResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mManualExitTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, String controllerPhysicalId, int slotNumber, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new RequestManualExitTask(sessionKey, controllerPhysicalId, slotNumber, taskDoneCallback));
    }

}