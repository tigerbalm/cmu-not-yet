package com.lge.notyet.driver.business;

import com.lge.notyet.channels.SignUpRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-15.
 */
public class SignUpTask implements Callable<Void> {


    private String mUserEmailAddress;
    private String mPassWord;
    private String mCreditCardNumber;
    private String mCreditCardExpireDate;
    private String mCreditCardCvc;
    private ITaskDoneCallback mTaskDoneCallback;

    public SignUpTask(String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
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

        sc.request(sc.createRequestMessage(mUserEmailAddress, mPassWord, mCreditCardNumber, mCreditCardExpireDate, mCreditCardCvc));
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mSignUpResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mSignUpResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mSignUpTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new SignUpTask(userEmailAddress, passWord, creditCardNumber, creditCardExpireDate, creditCardCvc, taskDoneCallback));
    }
}
