package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.ModifyAccountTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.driver.util.NumberUtils;
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

            String userEmailAddress = mTfUserEmailAddress.getText();
            String userPassword = new String(mTfUserPassword.getPassword());

            if (userEmailAddress == null || userEmailAddress.length() == 0) {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.INPUT_EMAIL_ADDRESS,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userPassword.length() == 0) {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.INPUT_PASSWORD,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String creditCardNumber = mTfCreditCardNumber.getText();
            String creditCardMonth = mTfCreditCardMonth.getText();
            String TfCreditCardYear = mTfCreditCardYear.getText();

            if (creditCardNumber == null || creditCardNumber.length() == 0
                    || !NumberUtils.isPositiveIntegerNumber(creditCardNumber)
                    || (creditCardNumber.length() != 15 && creditCardNumber.length() != 16)) {

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.INPUT_CREDIT_CARD_NUMBER + ", example: 1234567890123456",
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (creditCardMonth == null || creditCardMonth.length() == 0
                    || !NumberUtils.isPositiveIntegerNumber(creditCardMonth)
                    || creditCardMonth.length() != 2
                    || NumberUtils.toInt(creditCardMonth) < 0
                    || NumberUtils.toInt(creditCardMonth) > 12) {

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.INPUT_CREDIT_EXPIRE_DATE + ", example: 01/20" ,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (TfCreditCardYear == null || TfCreditCardYear.length() == 0
                    || NumberUtils.isNegativeIntegerNumber(TfCreditCardYear)
                    || TfCreditCardYear.length() != 2) {

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.INPUT_CREDIT_EXPIRE_DATE + ", example: 01/20" ,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            setUserInputEnabled(false);
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

        MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

        try {

            Log.logd(LOG_TAG, "Received response to ModifyAccount, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();

            } else if (success == 0) {

                Log.logd(LOG_TAG, "Failed to modify account, with cause=" + resMsg.getMessage().get("cause").asString());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MODIFY_ACCOUNT_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            } else {

                Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MODIFY_ACCOUNT_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
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
