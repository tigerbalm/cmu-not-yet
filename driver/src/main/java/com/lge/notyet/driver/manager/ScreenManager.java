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

    private LoginPanel mLoginPanel;
    private ReservationPanel mReservationPanel;
    private SignupPanel mSignupPanel;

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

        mCards.add(mLoginPanel.getRootPanel(), mLoginPanel.getName());
        mCards.add(mReservationPanel.getRootPanel(), mReservationPanel.getName());
        mCards.add(mSignupPanel.getRootPanel(), mSignupPanel.getName());

        showLoginScreen();

        return mCards;
    }

    public CardLayout getLayout() {
        return mMainCardLayout;
    }

    public void showLoginScreen() {
        mMainCardLayout.show(mCards, mLoginPanel.getName());
    }

    public void showReservationRequestScreen() {

        mReservationPanel.init();
        mMainCardLayout.show(mCards, mReservationPanel.getName());
    }

    public void showSignUpScreen() {

        mReservationPanel.init();
        mMainCardLayout.show(mCards, mSignupPanel.getName());
    }

}