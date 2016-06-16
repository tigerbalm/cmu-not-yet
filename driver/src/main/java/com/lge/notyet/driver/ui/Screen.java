package com.lge.notyet.driver.ui;

import javax.swing.*;

public interface Screen {

    void initScreen();
    void disposeScreen();

    JPanel getRootPanel();
    String getName();
}
