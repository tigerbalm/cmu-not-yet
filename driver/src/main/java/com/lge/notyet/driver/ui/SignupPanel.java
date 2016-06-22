package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.SignUpTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.driver.util.NumberUtils;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.crypto.SureParkCrypto;

import javax.swing.*;

public class SignupPanel implements Screen {

    private static final String LOG_TAG = "SignupPanel";

    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JTextField mTfCreditCardNumber;
    private JTextField mTfCreditCardMonth;
    private JTextField mTfCreditCardYear;
    private JButton mBtnCreateAccount;
    private JPanel mForm;
    private JButton mBtnCancel;

    @Override
    public void initScreen() {

        mTfUserEmailAddress.setText("");
        mTfUserPassword.setText("");
        mTfCreditCardNumber.setText("1122334455667788");
        mTfCreditCardMonth.setText("MM");
        mTfCreditCardYear.setText("YY");
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
            try {
                TaskManager.getInstance().runTask(SignUpTask.getTask(userEmailAddress, userPassword,
                        SureParkCrypto.encrypt(creditCardNumber),
                        SureParkCrypto.encrypt(creditCardMonth + "/" + TfCreditCardYear),
                        mSingUpDoneCallback));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Cancel
        mBtnCancel.addActionListener(e -> ScreenManager.getInstance().showLoginScreen());
    }

    private final ITaskDoneCallback mSingUpDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to sing up due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.SIGN_UP_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

        try {

            Log.logd(LOG_TAG, "Received response to SingUp, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                SessionManager.getInstance().clear();
                ScreenManager.getInstance().showLoginScreen();

            } else if (success == 0) {

                Log.logd(LOG_TAG, "Failed to sing up, with cause=" + resMsg.getMessage().get("cause").asString());

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.SIGN_UP_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();
            } else {

                Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.SIGN_UP_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to sing up, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.SIGN_UP_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();
        }
    };
}
