package com.lge.notyet.driver.manager;

import com.lge.notyet.driver.ui.*;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {

    private JPanel mCards;
    private final CardLayout mMainCardLayout;

    private Screen mLoginPanel;
    private Screen mReservationPanel;
    private Screen mReservationHistoryPanel;
    private Screen mSignupPanel;

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

        mCards.add(mLoginPanel.getRootPanel(), mLoginPanel.getName());
        mCards.add(mReservationPanel.getRootPanel(), mReservationPanel.getName());
        mCards.add(mSignupPanel.getRootPanel(), mSignupPanel.getName());
        mCards.add(mReservationHistoryPanel.getRootPanel(), mReservationHistoryPanel.getName());

        showLoginScreen();
        return mCards;
    }

    public void showLoginScreen() {
        mLoginPanel.initScreen();
        mMainCardLayout.show(mCards, mLoginPanel.getName());
    }

    public void showReservationRequestScreen() {
        mReservationPanel.initScreen();
        mMainCardLayout.show(mCards, mReservationPanel.getName());
    }

    public void showSignUpScreen() {
        mSignupPanel.initScreen();
        mMainCardLayout.show(mCards, mSignupPanel.getName());
    }

    public void showReservationHistoryScreen() {
        mReservationHistoryPanel.initScreen();
        mMainCardLayout.show(mCards, mReservationHistoryPanel.getName());
    }
}