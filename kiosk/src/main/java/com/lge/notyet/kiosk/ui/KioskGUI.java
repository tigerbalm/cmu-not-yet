package com.lge.notyet.kiosk.ui;

import com.lge.notyet.kiosk.ReservationNumber;
import com.lge.notyet.kiosk.comm.CommApi;
import com.lge.notyet.kiosk.comm.SerialComm2;
import com.lge.notyet.kiosk.comm.StatusListener;
import com.lge.notyet.kiosk.message.MyMessage;
import com.lge.notyet.kiosk.message.MyMessageBuilder;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sjun.lee on 2016-06-09.
 */
public class KioskGUI implements StatusListener {
    private JTextField reservationNum1;
    private JTextField reservationNum2;
    private JTextField reservationNum3;
    private JTextField reservationNum4;
    private JTextField[] numberFieldsGroup = new JTextField[]{reservationNum1, reservationNum2, reservationNum3, reservationNum4};

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
    private JButton[] numberButtonGroup = new JButton[] {
            btnNumber0, btnNumber1, btnNumber2, btnNumber3, btnNumber4, btnNumber5,
            btnNumber6, btnNumber7, btnNumber8, btnNumber9, btnNumberC
    };

    private JButton btnCheckReservation2;
    private JPanel kioskPanel;
    private JPanel monitorPanel;
    private JTextArea textSerialMonitor;
    private JButton btnConnect;
    private JLabel labelSerialInfo;
    private JTextField editMessage;
    private JButton btnSend;
    private JTextArea textMessage;
    private JCheckBox checkBoxScrollToEnd;

    ReservationNumber reservationNumber;
    CommApi comm;

    private KioskGUI() {
        initSerialComm();

        initReservationNumberField();

        initNumberButton();

        initCheckReservation();

        initSerialMonTextArea();

        initSerialCommStatusText();

        initConnectButton();

        initMessageSend();
    }

    private void initMessageSend() {
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textMessage.getText() + String.valueOf('\n');

                comm.send(message);

                //textMessage.setText("");
            }
        });
    }

    private void initSerialMonTextArea() {
        DefaultCaret caret = (DefaultCaret)textSerialMonitor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        resetMonitorArea(toReadableDate());

//        checkBoxScrollToEnd.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                if(e.getStateChange() == ItemEvent.SELECTED) {
//                    DefaultCaret caret = (DefaultCaret)textSerialMonitor.getCaret();
//                    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//                } else {
//                    DefaultCaret caret = (DefaultCaret)textSerialMonitor.getCaret();
//                    caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
//                }
//            }
//        });
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
        if (comm.connected()) {
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
                if (comm.connected()) {
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

                MyMessageBuilder message = MyMessageBuilder.builder()
                                                .topic("receive_book_no")
                                                .append("confirmation_no", reservationNumber.getAsString())
                                                .build();
                comm.send(message.toSerialProtocol());
            }
        });
    }

    private void initNumberButton() {
        for (JButton btn : numberButtonGroup) {
            if (btn == btnNumberC) {
                continue;
            }

            btn.addActionListener(new NumberListener());
        }

        btnNumberC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reservationNumber.removeLast();

                System.out.println("after remove : " + reservationNumber.get());
            }
        });

        numberButtonEnable(true);
    }

    private void numberButtonEnable(boolean enable) {
        System.out.println("numberButtonEnable : " + enable);

        for (JButton btn : numberButtonGroup) {
            btn.setEnabled(enable);
        }

        btnCheckReservation2.setEnabled(enable && numberFieldsFull());
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

            @Override
            public void onNumberTextRemoved(int index) {
                numberFieldsGroup[index].setText("-");

                btnCheckReservation2.setEnabled(false);
            }
        });
    }

    private boolean numberFieldsFull() {
        for (JTextField field : numberFieldsGroup) {
            if ("-".equals(field.getText())) {
                return false;
            }
        }

        return true;
    }

    private void initSerialComm() {
        comm = new SerialComm2(this);
        comm.connect();
    }

    @Override
    public void onConnected() {
        System.out.println("serial comm. connected");

        btnSend.setEnabled(true);
    }

    @Override
    public void onDisconnected() {
        System.out.println("serial comm. disconnected");

        btnSend.setEnabled(false);
    }

    @Override
    public void onMessageReceived(String message) {
        //System.out.println("message received: " + message.text());

        if (textSerialMonitor.getLineCount() > 50000) {
            textSerialMonitor.setText("");
        }

        textSerialMonitor.append(message);

        if (checkBoxScrollToEnd.isSelected()) {
            textSerialMonitor.setCaretPosition(textSerialMonitor.getDocument().getLength());
        }
    }

    // topic##body
    @Override
    public void onSystemMessageReceived(String message) {
        System.out.println("system message received: " + message);

        String[] splits = message.split("##");

        if ("error".equalsIgnoreCase(splits[0])) {
            JOptionPane.showMessageDialog(kioskPanel,
                    "<html><h1>" + splits[1] +"</html>",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        } else if ("control".equalsIgnoreCase(splits[0])) {
            if ("activate_kiosk".equalsIgnoreCase(splits[1])) {
                //btnCheckReservation2.setEnabled(true);
                //numberButtonEnable(true);
            } else if ("deactivate_kiosk".equalsIgnoreCase(splits[1])) {
                //btnCheckReservation2.setEnabled(false);
                //numberButtonEnable(false);
            }
        }
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
        JFrame frame = new JFrame("SurePark Kiosk");
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

