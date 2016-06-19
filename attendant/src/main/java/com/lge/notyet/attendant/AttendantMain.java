package com.lge.notyet.attendant;

import com.lge.notyet.attendant.manager.ScreenManager;

import javax.swing.*;

public class AttendantMain {

    private AttendantMain() {

        ScreenManager mScreenManager = ScreenManager.getInstance();
        JFrame frame = new JFrame("SurePark Attendant Manager");

        frame.getContentPane().add(mScreenManager.init());

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Start Driver Application
        new AttendantMain();
    }
}
