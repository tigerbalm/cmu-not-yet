package com.lge.notyet.driver;

/**
 * Created by beney.kim on 2016-06-12.
 */

import com.lge.notyet.driver.manager.ScreenManager;

import javax.swing.*;

public class DriverMain {

    private final ScreenManager mScreenManager;

    public DriverMain() {

        mScreenManager = ScreenManager.getInstance();
        JFrame frame = new JFrame("SurePark");

        frame.getContentPane().add(mScreenManager.init());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Start Driver Application
        new DriverMain();
    }

}

