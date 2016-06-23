package com.lge.notyet.ui;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonGateOPEN;
    private JButton buttonGateCLOSE;
    private JTextArea textArea1;

    private JPanel slot1;
    private JPanel slot2;
    private JPanel slot3;
    private JPanel slot4;

    public static Color SLOT_OCCUPIED_COLOR= Color.DARK_GRAY;
    public static Color SLOT_EMPTY_COLOR= Color.LIGHT_GRAY;
    public static JPanel uiParkingSlot[];

    private static String uniqueClientID="ClientPrototypeModule2";


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
        uiParkingSlot= new JPanel[]{slot1, slot2, slot3, slot4};
        for (JPanel uiParkingSlot_temp:uiParkingSlot) {
            uiParkingSlot_temp.setBackground(SLOT_EMPTY_COLOR);
        }

        ((DefaultCaret)textArea1.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
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
        dialog.setSize(300, 200);
        NetworkCommunicator.startService(uniqueClientID, dialog);

        dialog.setVisible(true);
        System.exit(0);
    }

    public void printToLog(String s) {
        textArea1.append(s+ "\r\n");

    }


    private void createUIComponents() {
        // HINT: place custom component creation code here
    }
}
