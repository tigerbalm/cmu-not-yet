package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ReservationCancelTask;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.business.SignUpTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class ReservationHistoryPanel {
    private JLabel mLabelUserName;
    private JLabel mLabelReservationDate;
    private JLabel mLabelReservationLocation;
    private JLabel mLabelReservationConfirmationNumber;
    private JPanel mForm;
    private JLabel mLabelModifyAccountInfo;
    private JButton cancelButton;

    public void init() {

        SessionManager mSessionManager = SessionManager.getInstance();

        mLabelUserName.setText("Dear " + mSessionManager.getUserEmail());

        Calendar reservedTime = Calendar.getInstance();
        reservedTime.setTimeInMillis(mSessionManager.getReservationTime()*1000);
        reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm, MM/dd/yyyy z");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String reservedTimeString = sdf.format(reservedTime.getTime());

        mLabelReservationDate.setText(reservedTimeString);

        int reservedFacilityId = mSessionManager.getReservationFacilityId();
        mLabelReservationLocation.setText(mSessionManager.getFacilityName(reservedFacilityId));

        mLabelReservationConfirmationNumber.setText(mSessionManager.getReservationConfirmationNumber() + "");
        //mLabelReservationConfirmationNumber.updateUI();
        //mLabelReservationConfirmationNumber.update(mLabelReservationConfirmationNumber.getGraphics());

    }

    public ReservationHistoryPanel() {
        mLabelModifyAccountInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });
        mLabelModifyAccountInfo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TaskManager.getInstance().runTask(ReservationCancelTask.getTask(SessionManager.getInstance().getKey(),
                        SessionManager.getInstance().getReservationId(),
                        mCancelReservationDoneCallback));
            }
        });
    }


    private ITaskDoneCallback mCancelReservationDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to cancel reservation due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to cancel reservation.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);
            System.out.println("Success to cancel reservation, response message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();

            } else if (resMsg.getResult() == 0) {
                System.out.println("Failed to signup, fail cause is " + resMsg.getFailCause());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to cancel reservation, fail cause=" + resMsg.getFailCause(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };


    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationHistoryPanel";
    }
}
