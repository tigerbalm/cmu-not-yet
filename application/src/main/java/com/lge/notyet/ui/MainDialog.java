package com.lge.notyet.ui;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.swing.*;
import java.awt.event.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonGateOPEN;
    private JButton buttonGateCLOSE;
    private JTextArea textArea1;
    static int counter=0;
    private static String serverURI="tcp://192.168.43.24";//"tcp://localhost";
    private static String uniqueClientID="ClientPrototypeModule";


    private MainDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonGateOPEN);

        buttonGateOPEN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGateOPEN();
            }
        });

        buttonGateCLOSE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGateCLOSE();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onGateOPEN() {
        NetworkCommunicator.sendMessageToBroker("1", "/facilities/1/gates/1");

    }

    private void onGateCLOSE() {
        NetworkCommunicator.sendMessageToBroker("0", "/facilities/1/gates/1");

    }


    public static void main(String[] args) {
        MainDialog dialog = new MainDialog();
        dialog.pack();
        NetworkCommunicator.startService(serverURI, uniqueClientID, dialog);

        dialog.setVisible(true);
        System.exit(0);
    }

    public void printToLog(String s) {
        textArea1.append(s+ Character.LINE_SEPARATOR);
    }
}
