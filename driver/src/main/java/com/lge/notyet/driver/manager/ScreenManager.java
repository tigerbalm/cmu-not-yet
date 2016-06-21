package com.lge.notyet.driver.manager;

import com.lge.notyet.driver.ui.*;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {

    private JPanel mCards;
    private final CardLayout mMainCardLayout;

    private String mLastScreenName;
    private String mCurrentScreenName;

    private Screen mLoginPanel;
    private Screen mReservationPanel;
    private Screen mReservationHistoryPanel;
    private Screen mSignupPanel;
    private Screen mModifyAccountPanel;

    private static ScreenManager sScreenManager = null;

    private ScreenManager () {
        mMainCardLayout = new CardLayout();
    }

    public static ScreenManager getInstance() {
        synchronized (ScreenManager.class) {
            if (sScreenManager == null) {
                sScreenManager = new ScreenManager();
            }
        }
        return sScreenManager;
    }

    public JPanel init() {

        mCards = new JPanel(mMainCardLayout);

        mLoginPanel = new LoginPanel();
        mReservationPanel = new ReservationPanel();
        mSignupPanel = new SignupPanel();
        mReservationHistoryPanel = new ReservationHistoryPanel();
        mModifyAccountPanel = new ModifyAccountPanel();

        mCards.add(mLoginPanel.getRootPanel(), mLoginPanel.getName());
        mCards.add(mReservationPanel.getRootPanel(), mReservationPanel.getName());
        mCards.add(mSignupPanel.getRootPanel(), mSignupPanel.getName());
        mCards.add(mReservationHistoryPanel.getRootPanel(), mReservationHistoryPanel.getName());
        mCards.add(mModifyAccountPanel.getRootPanel(), mModifyAccountPanel.getName());

        showLoginScreen();
        return mCards;
    }

    public void showLoginScreen() {
        mLastScreenName = null;
        mLoginPanel.initScreen();
        mMainCardLayout.show(mCards, mLoginPanel.getName());
        mCurrentScreenName = mLoginPanel.getName();
    }

    public void showReservationRequestScreen() {
        mLastScreenName = mCurrentScreenName;
        mReservationPanel.initScreen();
        mMainCardLayout.show(mCards, mReservationPanel.getName());
        mCurrentScreenName = mReservationPanel.getName();
    }

    public void showSignUpScreen() {
        mLastScreenName = mCurrentScreenName;
        mSignupPanel.initScreen();
        mMainCardLayout.show(mCards, mSignupPanel.getName());
        mCurrentScreenName = mSignupPanel.getName();
    }

    public void showReservationHistoryScreen() {
        mLastScreenName = mCurrentScreenName;
        mReservationHistoryPanel.initScreen();
        mMainCardLayout.show(mCards, mReservationHistoryPanel.getName());
        mCurrentScreenName = mReservationHistoryPanel.getName();
    }

    public void showModifyAccountPanelScreen() {
        mLastScreenName = mCurrentScreenName;
        mModifyAccountPanel.initScreen();
        mMainCardLayout.show(mCards, mModifyAccountPanel.getName());
        mCurrentScreenName = mModifyAccountPanel.getName();
    }

    public void showPreviousScreen() {
        if (mLastScreenName == null || mLastScreenName.length() == 0) return;
        mCurrentScreenName = mLastScreenName;
        mMainCardLayout.show(mCards, mLastScreenName);
        mLastScreenName = null;
    }

    public Screen getCurrentScreen() {

        if (mCurrentScreenName.equals(mLoginPanel.getName())) return mLoginPanel;
        if (mCurrentScreenName.equals(mReservationPanel.getName())) return mReservationPanel;
        if (mCurrentScreenName.equals(mReservationHistoryPanel.getName())) return mReservationHistoryPanel;
        if (mCurrentScreenName.equals(mSignupPanel.getName())) return mSignupPanel;
        if (mCurrentScreenName.equals(mModifyAccountPanel.getName())) return mModifyAccountPanel;
        return null;
    }
}