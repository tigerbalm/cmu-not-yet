package com.lge.notyet.owner.business;

import com.lge.notyet.owner.manager.NetworkConnectionManager;
import com.lge.notyet.owner.ui.ITaskDoneCallback;
import com.lge.notyet.owner.util.Log;
import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class LoginTask implements Callable<Void> {

    private static final String LOG_TAG = "LoginTask";

    private String mUserEmailAddress;
    private String mPassWord;
    private ITaskDoneCallback mTaskDoneCallback;

    public LoginTask(String userEmailAddress, String passWord, ITaskDoneCallback taskDoneCallback) {
        mUserEmailAddress = userEmailAddress;
        mPassWord = passWord;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        LoginRequestChannel lc = ncm.createLoginChannel();
        lc.addObserver(mLoginResult);
        lc.addTimeoutObserver(mLoginTimeout);

        MqttNetworkMessage requestMsg = lc.createRequestMessage(mUserEmailAddress, mPassWord);
        Log.log(LOG_TAG, requestMsg.toString());
        lc.request(requestMsg);
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mLoginResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mLoginResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mLoginTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String userEmailAddress, String passWord, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new LoginTask(userEmailAddress, passWord, taskDoneCallback));
    }
}
