package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.manager.ScreenManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class LoginPanel {
    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JLabel mLabelCreateAccount;
    private JLabel mLabelForgetPassword;

    public LoginPanel() {
        mBtnSignIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreenManager.getInstance().showReservationScreen();
            }
        });
    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "LoginScreen";
    }
}
