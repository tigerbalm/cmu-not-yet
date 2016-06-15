package com.lge.notyet.driver.ui;

import javax.swing.*;

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

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "SignupPanel";
    }
}
