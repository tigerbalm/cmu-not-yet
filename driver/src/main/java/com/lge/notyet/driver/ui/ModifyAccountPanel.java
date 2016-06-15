package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

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

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ModifyAccountPanel";
    }
}
