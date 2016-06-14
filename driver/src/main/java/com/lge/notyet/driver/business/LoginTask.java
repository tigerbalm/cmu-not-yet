package com.lge.notyet.driver.business;

import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class LoginTask implements Callable<Void> {

    private String mUserEmailAddress;
    private String mPassWord;
    private ITaskDoneCallback mTaskDoneCallback;

    public LoginTask(String userEmailAddress, String passWord, ITaskDoneCallback taskDoneCallback) {
        mUserEmailAddress = passWord;
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

        // ReservationRequestMessage message = new ReservationRequestMessage().setSessionKey(/*mSessionKey*/ "ssssss").setTimeStamp(mRequestTime);
        // lc.request(message);
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
