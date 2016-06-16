package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.ReservationCancelTask;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ReservationHistoryPanel implements Screen {

    private static final String LOG_TAG = "ReservationHistoryPanel";

    private JLabel mLabelUserName;
    private JLabel mLabelReservationDate;
    private JLabel mLabelReservationLocation;
    private JLabel mLabelReservationConfirmationNumber;
    private JPanel mForm;
    private JLabel mLabelModifyAccountInfo;
    private JButton cancelButton;
    private JLabel mLabelLogout;


    @Override
    public void initScreen() {

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

    @Override
    public void disposeScreen() {

    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationHistoryPanel";
    }

    private void setUserInputEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    public ReservationHistoryPanel() {

        // Modify Account
        mLabelModifyAccountInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });

        // Modify Account
        mLabelModifyAccountInfo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                ScreenManager.getInstance().showModifyAccountPanelScreen();
            }
        });

        // Log-out
        mLabelLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SessionManager.getInstance().clear(); // Log-out
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });

        // Log-out
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                SessionManager.getInstance().clear(); // Log-out
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });

        // Cancel Reservation
        cancelButton.addActionListener(e -> {

            setUserInputEnabled(false);
            TaskManager.getInstance().runTask(ReservationCancelTask.getTask(SessionManager.getInstance().getKey(),
                    SessionManager.getInstance().getReservationId(),
                    mCancelReservationDoneCallback));
        });
    }

    private final ITaskDoneCallback mCancelReservationDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to cancel reservation due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.CANCEL_RESERVATION_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReservationResponseMessage resMsg = new ReservationResponseMessage((MqttNetworkMessage)response);

        try {

            Log.logd(LOG_TAG, "Received response to CancelReservation, message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();

            } else if (resMsg.getResult() == 0) {

                Log.logd(LOG_TAG, "Failed to signup, with cause=" + resMsg.getFailCause());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.CANCEL_RESERVATION_FAILED + ":" + resMsg.getFailCause(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to cancel reservation due to timeout");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.CANCEL_RESERVATION_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    };
}
