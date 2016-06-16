package com.lge.notyet.driver;

import com.lge.notyet.driver.manager.ScreenManager;

import javax.swing.*;

public class DriverMain {

    private DriverMain() {

        ScreenManager mScreenManager = ScreenManager.getInstance();
        JFrame frame = new JFrame("SurePark");

        frame.getContentPane().add(mScreenManager.init());

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Start Driver Application
        new DriverMain();
    }

}

