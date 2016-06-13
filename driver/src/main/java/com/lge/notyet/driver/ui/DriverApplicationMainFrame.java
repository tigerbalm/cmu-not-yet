package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ReservationTask;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.NetworkMessage;

import javax.swing.*;
import java.awt.event.*;

public class DriverApplicationMainFrame extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField userNameTextField;
    private JPasswordField passwordField1;
    private JButton signInButton;

    public DriverApplicationMainFrame() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        /*
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
*/
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
    }

    ITaskDoneCallback mReservationDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {
            System.out.println("result=" + result + ", message=" + ((NetworkMessage)response).getMessage());
        }
    };

    private void onOK() {
// add your code here
        TaskManager.getInstance().runTask(ReservationTask.getTask(0, mReservationDoneCallback));
        //dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
