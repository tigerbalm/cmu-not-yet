package com.lge.notyet.attendant.ui;

import javax.swing.*;

public interface Screen {

    void initScreen();
    void disposeScreen();

    JPanel getRootPanel();
    String getName();
}
