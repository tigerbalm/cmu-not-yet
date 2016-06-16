package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.business.SignUpTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;

public class SignupPanel implements Screen {

    private static final String LOG_TAG = "SignupPanel";

    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JTextField mTfCreditCardNumber;
    private JTextField mTfCreditCardMonth;
    private JTextField mTfCreditCardYear;
    private JButton mBtnCreateAccount;
    private JTextField mTfCreditCardCVC;
    private JPanel mForm;
    private JButton mBtnCancel;

    @Override
    public void initScreen() {

    }

    @Override
    public void disposeScreen() {

    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "SignupPanel";
    }

    private void setUserInputEnabled(boolean enabled) {

        mTfUserEmailAddress.setEnabled(enabled);
        mTfUserPassword.setEnabled(enabled);
        mTfCreditCardNumber.setEnabled(enabled);
        mTfCreditCardMonth.setEnabled(enabled);
        mTfCreditCardYear.setEnabled(enabled);
        mBtnCancel.setEnabled(enabled);
        mBtnCreateAccount.setEnabled(enabled);
        // mTfCreditCardCVC.setEnabled(enabled);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    public SignupPanel() {

        // Sign up
        mBtnCreateAccount.addActionListener(e -> {

            setUserInputEnabled(false);

            String userEmailAddress = mTfUserEmailAddress.getText();
            String userPassword = new String(mTfUserPassword.getPassword());

            TaskManager.getInstance().runTask(SignUpTask.getTask(userEmailAddress, userPassword,
                    mTfCreditCardNumber.getText(),
                    mTfCreditCardMonth.getText() + "/" + mTfCreditCardYear.getText(),
                    mTfCreditCardCVC.getText(),
                    mSingUpDoneCallback));
        });

        // Cancel
        mBtnCancel.addActionListener(e -> ScreenManager.getInstance().showLoginScreen());
    }

    private final ITaskDoneCallback mSingUpDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to sing up due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.SIGN_UP_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);

        try {

            Log.logd(LOG_TAG, "Received response to SingUp, message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();

            } else if (resMsg.getResult() == 0) {

                Log.logd(LOG_TAG, "Failed to sing up, with cause=" + resMsg.getFailCause());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.SIGN_UP_FAILED + ":" + resMsg.getFailCause(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to sing up, exception occurred");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.SIGN_UP_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    };
}
