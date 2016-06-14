package com.lge.notyet.driver.ui;

import com.lge.notyet.channels.ReservationResponseMessage;
import com.lge.notyet.driver.business.ReservationTask;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ReservationPanel {

    private JButton mBtnMakeReservation;
    private JPanel mForm;
    private JLabel mLabelUserName;
    private JComboBox mCbLocation;
    private JTextField mTfCreditCardNumber;
    private JSpinner mJSpinnerHour;

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationPanel";
    }

    public void init() {

        Calendar calNewYork = Calendar.getInstance();
        calNewYork.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        Calendar maxTime = Calendar.getInstance();
        maxTime.add(Calendar.HOUR_OF_DAY, 3);
        maxTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        Calendar minTime = Calendar.getInstance();
        minTime.add(Calendar.MINUTE, -1);
        minTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));


        SpinnerDateModel model = (SpinnerDateModel)mJSpinnerHour.getModel();
        SimpleDateFormat format = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getFormat();
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        model.setValue(calNewYork.getTime());
        model.setStart(minTime.getTime());
        model.setEnd(maxTime.getTime());
    }

    public ReservationPanel() {

        mBtnMakeReservation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Long selectedTime = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getModel().getDate().getTime()/1000;
                TaskManager.getInstance().runTask(ReservationTask.getTask(0, selectedTime, mReservationDoneCallback));
                mBtnMakeReservation.setEnabled(false);
            }
        });
    }

    private ITaskDoneCallback mReservationDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            mBtnMakeReservation.setEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to make reservation due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to make reservation.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);

            if (resMsg.getResult() == 1) { // Success
                System.out.println("Success to make reservation, confirmation number is " + resMsg.getConfirmationNumber());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Your reservation number is:" + resMsg.getConfirmationNumber(),
                        "SurePark",
                        JOptionPane.PLAIN_MESSAGE);

            } else if (resMsg.getResult() == 0) {
                System.out.println("Failed to make reservation, fail cause is " + resMsg.getFailCause());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to make reservation, fail cause=" + resMsg.getFailCause(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    private void createUIComponents() {
        // TODO: place custom component creation code here
        final SpinnerDateModel model = new SpinnerDateModel();
        mJSpinnerHour = new JSpinner(model);
    }
}
