package com.lge.notyet.driver.manager;

/**
 * Created by beney.kim on 2016-06-12.
 */

import com.lge.notyet.driver.ui.*;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {

    private static ScreenManager sScreenManager = null;
    private CardLayout mMainCardLayout;
    private JPanel mCards;

    private String mLastScreenName;
    private String mCurrentScreenName;

    private LoginPanel mLoginPanel;
    private ReservationPanel mReservationPanel;
    private SignupPanel mSignupPanel;
    private ModifyAccountPanel mModifyAccountPanel;
    private ReservationHistoryPanel mReservationHistoryPanel;

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

    public CardLayout getLayout() {
        return mMainCardLayout;
    }

    public void showLoginScreen() {
        mLastScreenName = mCurrentScreenName;
        mMainCardLayout.show(mCards, mLoginPanel.getName());
        mCurrentScreenName = mLoginPanel.getName();
    }

    public void showReservationRequestScreen() {
        mLastScreenName = mCurrentScreenName;
        mReservationPanel.init();
        mMainCardLayout.show(mCards, mReservationPanel.getName());
        mCurrentScreenName = mReservationPanel.getName();
    }

    public void showSignUpScreen() {
        mLastScreenName = mCurrentScreenName;
        mMainCardLayout.show(mCards, mSignupPanel.getName());
        mCurrentScreenName = mSignupPanel.getName();
    }

    public void showReservationHistoryScreen() {
        mLastScreenName = mCurrentScreenName;
        mReservationHistoryPanel.init();
        mMainCardLayout.show(mCards, mReservationHistoryPanel.getName());
        mCurrentScreenName = mReservationHistoryPanel.getName();
    }

    public void showModifyAccountPanelScreen() {
        mLastScreenName = mCurrentScreenName;
        mModifyAccountPanel.init();
        mMainCardLayout.show(mCards, mModifyAccountPanel.getName());
        mCurrentScreenName = mModifyAccountPanel.getName();
    }


    public void showPreviousScreen() {

        if (mLastScreenName == null || mLastScreenName.length() == 0) return;

        mCurrentScreenName = mLastScreenName;
        mMainCardLayout.show(mCards, mLastScreenName);
        mLastScreenName = null;
    }
}