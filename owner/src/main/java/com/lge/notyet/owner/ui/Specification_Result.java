package com.lge.notyet.owner.ui;

import com.lge.notyet.owner.business.Networking;
import com.lge.notyet.owner.business.StateMachine;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Specification_Result extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField startDate;
    private JTextField endDate;
    private JTextArea resultArea;

    public Specification_Result() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        resultArea.setText(StateMachine.getInstance().getQuery()+"\r\n");
        Networking.getInstance().queryServer(resultArea, StateMachine.getInstance().getSqlQuery() );
    }


}
