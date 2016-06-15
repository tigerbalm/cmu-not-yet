package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.business.SignUpTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class SignupPanel {
    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JTextField mTfCreditCardNumber;
    private JTextField mTfCreditCardMonth;
    private JTextField mTfCreditCardYear;
    private JButton mBtnCreateAccount;
    private JTextField mTfCreditCardCVC;
    private JPanel mForm;
    private JButton mBtnCancel;

    public SignupPanel() {
        mBtnCreateAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                TaskManager.getInstance().runTask(SignUpTask.getTask(mTfUserEmailAddress.getText(), mTfUserPassword.getPassword().toString(),
                        mTfCreditCardNumber.getText(), mTfCreditCardMonth.getText() + "/" + mTfCreditCardYear.getText(), mTfCreditCardCVC.getText(), mSingUpDoneCallback));
                mTfUserEmailAddress.setEnabled(false);
                mTfUserPassword.setEnabled(false);
                mBtnCreateAccount.setEnabled(false);
            }
        });
        mBtnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreenManager.getInstance().showLoginScreen();
            }
        });
    }


    private ITaskDoneCallback mSingUpDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            mTfUserEmailAddress.setEnabled(true);
            mTfUserPassword.setEnabled(true);
            mBtnCreateAccount.setEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to signup due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to signup.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);
            System.out.println("Success to signup, response message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                ScreenManager.getInstance().showLoginScreen();

            } else if (resMsg.getResult() == 0) {
                System.out.println("Failed to signup, fail cause is " + resMsg.getFailCause());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to signup, fail cause=" + resMsg.getFailCause(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "SignupPanel";
    }
}
