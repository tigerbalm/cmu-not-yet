package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.manager.ScreenManager;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class ReservationHistoryPanel {
    private JLabel mLabelUserName;
    private JLabel mLabelReservationDate;
    private JLabel mLabelReservationLocation;
    private JLabel mLabelReservationConfirmationNumber;
    private JPanel mForm;
    private JLabel mLabelModifyAccountInfo;

    public ReservationHistoryPanel() {
        mLabelModifyAccountInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });
        mLabelModifyAccountInfo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });
    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationHistoryPanel";
    }
}
