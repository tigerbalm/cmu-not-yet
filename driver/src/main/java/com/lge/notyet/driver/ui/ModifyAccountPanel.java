package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.ModifyAccountTask;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.util.StringTokenizer;

public class ModifyAccountPanel implements Screen {

    private static final String LOG_TAG = "ModifyAccountPanel";

    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JTextField mTfCreditCardNumber;
    private JTextField mTfCreditCardMonth;
    private JTextField mTfCreditCardYear;
    private JTextField mTfCreditCardCVC;
    private JButton mBtnModifyAccount;
    private JButton mBtnCancel;

    @Override
    public void initScreen() {

        SessionManager mSessionManager = SessionManager.getInstance();

        mTfUserEmailAddress.setText(mSessionManager.getUserEmail());
        mTfCreditCardNumber.setText(mSessionManager.getCreditCardNumber());

        String creditCardExpireDate = mSessionManager.getCreditCardExpireDate();
        StringTokenizer creditCardExpireDateTokenizer = new StringTokenizer(creditCardExpireDate, "/");
        if (creditCardExpireDateTokenizer.countTokens() == 2) {
            mTfCreditCardMonth.setText(creditCardExpireDateTokenizer.nextToken());
            mTfCreditCardYear.setText(creditCardExpireDateTokenizer.nextToken());
        }
    }

    @Override
    public void disposeScreen() {

    }

    @Override
    public JPanel getRootPanel() {
        return mForm;
    }

    @Override
    public String getName() {
        return "ModifyAccountPanel";
    }

    private void setUserInputEnabled(boolean enabled) {

        mTfUserEmailAddress.setEnabled(enabled);
        mTfUserPassword.setEnabled(enabled);
        mTfCreditCardNumber.setEnabled(enabled);
        mTfCreditCardMonth.setEnabled(enabled);
        mTfCreditCardYear.setEnabled(enabled);
        mBtnCancel.setEnabled(enabled);
        mBtnModifyAccount.setEnabled(enabled);
        // mTfCreditCardCVC.setEnabled(enabled);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    public ModifyAccountPanel() {

        // Cancel
        mBtnCancel.addActionListener(e -> ScreenManager.getInstance().showPreviousScreen());

        // Modify Account
        mBtnModifyAccount.addActionListener(e -> {

            setUserInputEnabled(false);

            String userEmailAddress = mTfUserEmailAddress.getText();
            String userPassword = new String(mTfUserPassword.getPassword());

            TaskManager.getInstance().runTask(ModifyAccountTask.getTask(
                    SessionManager.getInstance().getKey(),
                    userEmailAddress, userPassword,
                    mTfCreditCardNumber.getText(),
                    mTfCreditCardMonth.getText() + "/" + mTfCreditCardYear.getText(),
                    mTfCreditCardCVC.getText(),
                    mModifyAccountCallback));
        });
    }

    private final ITaskDoneCallback mModifyAccountCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to modify account due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.MODIFY_ACCOUNT_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);

        try {

            Log.logd(LOG_TAG, "Received response to ModifyAccount, message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();

            } else if (resMsg.getResult() == 0) {

                Log.logd(LOG_TAG, "Failed to modify account, with cause=" + resMsg.getFailCause());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MODIFY_ACCOUNT_FAILED + ":" + resMsg.getFailCause(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to modify account, exception occurred");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.MODIFY_ACCOUNT_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    };
}
