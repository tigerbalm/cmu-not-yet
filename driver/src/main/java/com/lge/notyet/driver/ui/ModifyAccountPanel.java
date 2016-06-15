package com.lge.notyet.driver.ui;

import javax.swing.*;

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
    private JButton mBtnCreateAccount;

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ModifyAccountPanel";
    }
}
