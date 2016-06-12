package com.lge.notyet.driver.ui;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.lib.comm.INetworkCallback;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkConnection;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
import java.net.InetAddress;

public class DriverApplicationMainFrame extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    public DriverApplicationMainFrame() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        mNc = new MqttNetworkConnection(null);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
        }

        @Override
        public void onConnectFailed() {
        }

        @Override
        public void onLost() {
            mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
        }
    };
    private void onOK() {
// add your code here
        ReservationRequestChannel mReservationRequestChannel = ReservationRequestChannel.build(mNc, 1);
        MqttNetworkMessage data = new MqttNetworkMessage(new JsonObject().add("RESERVATION", "Beney"));
        mReservationRequestChannel.request(data);
        //dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
