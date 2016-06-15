package com.lge.notyet.attendant.manager;

/**
 * Created by beney.kim on 2016-06-12.
 */

import com.lge.notyet.attendant.ui.FacilityMonitorPanel;
import com.lge.notyet.attendant.ui.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {

    private static ScreenManager sScreenManager = null;
    private CardLayout mMainCardLayout;
    private JPanel mCards;

    private String mLastScreenName;
    private String mCurrentScreenName;

    private LoginPanel mLoginPanel;
    private FacilityMonitorPanel mFacilityMonitorPanel;

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
        mFacilityMonitorPanel = new FacilityMonitorPanel();

        mCards.add(mLoginPanel.getRootPanel(), mLoginPanel.getName());
        mCards.add(mFacilityMonitorPanel.getRootPanel(), mFacilityMonitorPanel.getName());

        showLoginScreen();

        return mCards;
    }

    public CardLayout getLayout() {
        return mMainCardLayout;
    }

    public void showLoginScreen() {
        mLastScreenName = null;
        mLoginPanel.init();
        mMainCardLayout.show(mCards, mLoginPanel.getName());
        mCurrentScreenName = mLoginPanel.getName();
    }

    public void showFacilityMonitorScreen() {
        mLastScreenName = mCurrentScreenName;
        mFacilityMonitorPanel.init();
        mMainCardLayout.show(mCards, mFacilityMonitorPanel.getName());
        mCurrentScreenName = mFacilityMonitorPanel.getName();
    }
}