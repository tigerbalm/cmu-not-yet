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
    static int counter=0;
    private static String serverURI="tcp://192.168.43.24";//"tcp://localhost";

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
                onGateCLOSE();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGateCLOSE();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onGateOPEN() {
        sendMessageToBroker("1", "/facilities/1/gates/1");
    }

    private void onGateCLOSE() {
        sendMessageToBroker("0", "/facilities/1/gates/1");

    }

    public static void main(String[] args) {
        MainDialog dialog = new MainDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private String sendMessageToBroker(String messageString, String topicId){
        String returnMessage=null;
        try {
            MqttClient client;
            client = new MqttClient(serverURI, "ClientPrototypeModule");
            client.connect();
            MqttMessage message = new MqttMessage();
            message.setPayload(messageString.getBytes());
            client.publish(topicId, message);
            client.disconnect();
        } catch (MqttException e2) {
            e2.printStackTrace();
        }
        return returnMessage;
    }


}
