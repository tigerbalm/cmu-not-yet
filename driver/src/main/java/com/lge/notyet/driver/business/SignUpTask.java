package com.lge.notyet.driver.business;

import com.lge.notyet.channels.SignUpRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class SignUpTask implements Callable<Void> {

    private static final String LOG_TAG = "SignUpTask";

    private final String mUserEmailAddress;
    private final String mPassWord;
    private final String mCreditCardNumber;
    private final String mCreditCardExpireDate;
    private final String mCreditCardCvc;
    private final ITaskDoneCallback mTaskDoneCallback;

    private SignUpTask(String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
        mUserEmailAddress = userEmailAddress;
        mPassWord = passWord;
        mCreditCardNumber = creditCardNumber;
        mCreditCardExpireDate = creditCardExpireDate;
        mCreditCardCvc = creditCardCvc;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        SignUpRequestChannel sc = ncm.createSignUpChannel();
        sc.addObserver(mSignUpResult);
        sc.addTimeoutObserver(mSignUpTimeout);

        boolean ret = sc.request(SignUpRequestChannel.createRequestMessage(mUserEmailAddress, mPassWord, mCreditCardNumber, mCreditCardExpireDate, mCreditCardCvc));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mSignUpResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logd(LOG_TAG, "mSignUpResult Result=" + message.getMessage());
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mSignUpTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new SignUpTask(userEmailAddress, passWord, creditCardNumber, creditCardExpireDate, creditCardCvc, taskDoneCallback));
    }
}
