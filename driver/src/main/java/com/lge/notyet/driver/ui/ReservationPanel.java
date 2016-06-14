package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ReservationTask;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.NetworkMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class ReservationPanel {

    private JComboBox mCbData;
    private JComboBox mCbTime;
    private JComboBox mCbLocation;
    private JTextField mTfCreditCardNumber;
    private JButton mBtnMakeReservation;
    private JPanel mForm;
    private JLabel mLabelUserName;

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationPanel";
    }

    public ReservationPanel() {

        mBtnMakeReservation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TaskManager.getInstance().runTask(ReservationTask.getTask(0, mReservationDoneCallback));
            }
        });
    }

    ITaskDoneCallback mReservationDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {
            System.out.println("result=" + result + ", message=" + ((NetworkMessage)response).getMessage());
        }
    };
}
