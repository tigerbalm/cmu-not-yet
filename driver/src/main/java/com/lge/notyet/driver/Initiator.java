package com.lge.notyet.driver;

/**
 * Created by beney.kim on 2016-06-12.
 */

import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.ui.*;

public class Initiator {

    public static void main(String[] args) {
        DriverApplicationMainFrame dialog = new DriverApplicationMainFrame();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
