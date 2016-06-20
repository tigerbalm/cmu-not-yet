package com.lge.notyet.attendant;

import com.lge.notyet.attendant.manager.ScreenManager;
import sun.awt.AppContext;

import javax.swing.*;
import java.util.Locale;

public class AttendantMain {

    private AttendantMain() {

        ScreenManager mScreenManager = ScreenManager.getInstance();
        JFrame frame = new JFrame("SurePark Attendant Manager");

        frame.getContentPane().add(mScreenManager.init());

        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);
        AppContext.getAppContext().put("JComponent.defaultLocale", locale);

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
