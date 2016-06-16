package com.lge.notyet.kiosk.ui;

import com.lge.notyet.kiosk.ReservationNumber;
import com.lge.notyet.kiosk.SerialComm;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * Created by sjun.lee on 2016-06-09.
 */
public class KioskGUI implements SerialComm.MessageUpdatable {
    private JTextField reservationNum1;
    private JTextField reservationNum2;
    private JTextField reservationNum3;
    private JTextField reservationNum4;
    private JTextField[] numberFieldsGroup = new JTextField[] {reservationNum1, reservationNum2, reservationNum3, reservationNum4};

    private JButton btnNumber1;
    private JButton btnNumber2;
    private JButton btnNumber3;
    private JButton btnNumber4;
    private JButton btnNumber5;
    private JButton btnNumber6;
    private JButton btnNumber7;
    private JButton btnNumber8;
    private JButton btnNumber9;
    private JButton btnNumber0;
    private JButton btnNumberC;

    private JButton btnCheckReservation2;
    private JPanel kioskPanel;
    private JPanel monitorPanel;
    private JTextArea textSerialMonitor;
    private JButton btnConnect;
    private JLabel labelSerialInfo;

    ReservationNumber reservationNumber;
    SerialComm comm;

    private KioskGUI() {
        initSerialComm();

        initReservationNumberField();

        initNumberButton();

        initCheckReservation();

        initSerialMonTextArea();

        initSerialCommStatusText();

        initConnectButton();
    }

    private void initSerialMonTextArea() {
        DefaultCaret caret = (DefaultCaret)textSerialMonitor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        resetMonitorArea(toReadableDate());
    }

    private void resetMonitorArea(String t) {
        textSerialMonitor.setText(t);
        textSerialMonitor.append("\n----------------------------------------------------\n");
    }

    private String toReadableDate() {
        DateFormat format = SimpleDateFormat.getInstance();

        return format.format(new Date());
    }

    private void initSerialCommStatusText() {
        if (comm.isConnected()) {
            btnConnect.setText("Disconnect");
        } else {
            btnConnect.setText("Connect");
        }

        labelSerialInfo.setText(comm.toString());
    }

    private void initConnectButton() {
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("btnConnect is clicked...");
                if (comm.isConnected()) {
                    comm.disconnect();
                } else {
                    resetMonitorArea(toReadableDate());
                    comm.connect();
                }

                initSerialCommStatusText();
            }
        });
    }

    private void initCheckReservation() {
        btnCheckReservation2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("you input : " + reservationNumber.get());
                comm.send(reservationNumber.getAsString().getBytes());
            }
        });
    }

    private void initNumberButton() {
        btnNumber0.addActionListener(new NumberListener());
        btnNumber1.addActionListener(new NumberListener());
        btnNumber2.addActionListener(new NumberListener());
        btnNumber3.addActionListener(new NumberListener());
        btnNumber4.addActionListener(new NumberListener());
        btnNumber5.addActionListener(new NumberListener());
        btnNumber6.addActionListener(new NumberListener());
        btnNumber7.addActionListener(new NumberListener());
        btnNumber8.addActionListener(new NumberListener());
        btnNumber9.addActionListener(new NumberListener());
        btnNumberC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reservationNumber.removeLast();

                System.out.println("after remove : " + reservationNumber.get());
            }
        });
    }

    private void initReservationNumberField() {
        reservationNumber = new ReservationNumber();
        reservationNumber.setNumberTextChangedListener(new ReservationNumber.NumberTextChangedListener() {
            @Override
            public void onNumberTextChanged(int index, String number) {
                numberFieldsGroup[index].setText(number);

                if (numberFieldsFull()) {
                    btnCheckReservation2.setEnabled(true);
                }
            }

            private boolean numberFieldsFull() {
                for (JTextField field : numberFieldsGroup) {
                    if ("-".equals(field.getText())) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void onNumberTextRemoved(int index) {
                numberFieldsGroup[index].setText("-");

                btnCheckReservation2.setEnabled(false);
            }
        });
    }

    private void initSerialComm() {
        comm = new SerialComm(this);
        comm.connect();
    }

    @Override
    public void onMessageUpdate(String message) {
        if (textSerialMonitor.getLineCount() > 50000) {
            textSerialMonitor.setText("");
        }

        textSerialMonitor.append(message);
    }

    class NumberListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = ((JButton)e.getSource()).getText();
            System.out.println("number: " + input);
            reservationNumber.addLast(input);

            System.out.println("after input : " + reservationNumber.get());
        }
    }

    private Container getMainPanel() {
        return kioskPanel;
    }

    public static void showGui() {
        KioskGUI kiosk = new KioskGUI();
        JFrame frame = new JFrame("Calculator");
        frame.setContentPane(kiosk.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);

                kiosk.guiClosed();
            }
        });
    }

    private void guiClosed() {
        comm.disconnect();
    }
}

