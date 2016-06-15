package com.lge.notyet.owner.business;

import com.lge.notyet.owner.ui.Specification_Result;

import javax.swing.*;
import java.awt.*;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class Networking {
    private static Networking ourInstance = new Networking();

    public static Networking getInstance() {
        return ourInstance;
    }

    private Networking() {
    }

    public void queryServer(Component callingUI, String query) {
        JOptionPane.showMessageDialog(callingUI, query);
    }

    public class mySQLStub{

        public mySQLStub() {
            //MysqlDataSource
        }
    }
}
