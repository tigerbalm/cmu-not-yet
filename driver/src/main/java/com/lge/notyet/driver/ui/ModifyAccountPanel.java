package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ModifyAccountTask;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class ModifyAccountPanel {

    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JTextField mTfCreditCardNumber;
    private JTextField mTfCreditCardMonth;
    private JTextField mTfCreditCardYear;
    private JTextField mTfCreditCardCVC;
    private JButton mBtnModifyAccount;
    private JButton mBtnCancel;

    public ModifyAccountPanel() {
        mBtnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreenManager.getInstance().showPreviousScreen();
            }
        });
        mBtnModifyAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                TaskManager.getInstance().runTask(ModifyAccountTask.getTask(
                        SessionManager.getInstance().getKey(),
                        mTfUserEmailAddress.getText(), mTfUserPassword.getPassword().toString(),
                        mTfCreditCardNumber.getText(), mTfCreditCardMonth.getText() + "/" + mTfCreditCardYear.getText(), mTfCreditCardCVC.getText(), mModifyAccountCallback));

                mTfUserEmailAddress.setEnabled(false);
                mTfUserPassword.setEnabled(false);
                mBtnModifyAccount.setEnabled(false);
            }
        });
    }

    public void init() {

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


    private ITaskDoneCallback mModifyAccountCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            mTfUserEmailAddress.setEnabled(true);
            mTfUserPassword.setEnabled(true);
            mBtnModifyAccount.setEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to modify account due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to modify account.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);
            System.out.println("Success to modify account, response message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                SessionManager.getInstance().clear(); // Log-out
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();

            } else if (resMsg.getResult() == 0) {
                System.out.println("Failed to signup, fail cause is " + resMsg.getFailCause());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to modify account, fail cause=" + resMsg.getFailCause(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ModifyAccountPanel";
    }
}
