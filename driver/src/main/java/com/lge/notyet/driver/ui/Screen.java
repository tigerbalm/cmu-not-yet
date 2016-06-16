package com.lge.notyet.driver.ui;

import javax.swing.*;

/**
 * Created by beney.kim on 2016-06-17.
 */

public interface Screen {

    void initScreen();
    void disposeScreen();

    JPanel getRootPanel();
    String getName();
}
