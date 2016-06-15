package com.lge.notyet.attendant;

import com.lge.notyet.attendant.manager.ScreenManager;

import javax.swing.*;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class AttendantMain {

    private final ScreenManager mScreenManager;

    public AttendantMain() {

        mScreenManager = ScreenManager.getInstance();
        JFrame frame = new JFrame("SurePark Attendant Manager");

        frame.getContentPane().add(mScreenManager.init());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Start Driver Application
        new AttendantMain();
    }
}
