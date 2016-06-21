package com.lge.notyet.driver.ui;

import com.lge.notyet.channels.ControllerStatusSubscribeChannel;
import com.lge.notyet.channels.ReservationStatusSubscribeChannel;
import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.ReservationCancelTask;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.IOnNotify;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class ReservationHistoryPanel implements Screen {

    private static final String LOG_TAG = "ReservationHistoryPanel";

    private JLabel mLabelUserName;
    private JLabel mLabelReservationDate;
    private JLabel mLabelReservationLocation;
    private JLabel mLabelReservationConfirmationNumber;
    private JPanel mForm;
    private JLabel mLabelModifyAccountInfo;
    private JButton mBtnCancelReservation;
    private JLabel mLabelLogout;

    private ReservationStatusSubscribeChannel mReservationStatusSubscribeChannel = null;
    private ControllerStatusSubscribeChannel mControllerStatusSubscribeChannel = null;

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

        if (SessionManager.getInstance().getUnderTransaction()) {
            mBtnCancelReservation.setEnabled(false);
        } else {
            mBtnCancelReservation.setEnabled(true);
        }

        subscribeEvents();
    }

    @Override
    public void disposeScreen() {

        unsubscribeEvents();
    }

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationHistoryPanel";
    }

    private void setUserInputEnabled(boolean enabled) {
        mBtnCancelReservation.setEnabled(enabled);
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
                unsubscribeEvents();
                SessionManager.getInstance().clear(); // Log-out
                // NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });

        // Log-out
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                unsubscribeEvents();
                SessionManager.getInstance().clear(); // Log-out
                // NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });

        // Cancel Reservation
        mBtnCancelReservation.addActionListener(e -> {

            setUserInputEnabled(false);
            TaskManager.getInstance().runTask(ReservationCancelTask.getTask(SessionManager.getInstance().getKey(),
                    SessionManager.getInstance().getReservationId(),
                    mCancelReservationDoneCallback));
        });
    }

    private void subscribeEvents() {

        int reservationId = SessionManager.getInstance().getReservationId();

        unsubscribeEvents();

        if (reservationId != -1) {
            mReservationStatusSubscribeChannel = NetworkConnectionManager.getInstance().createReservationStatusSubscribeChannel(reservationId);
            mReservationStatusSubscribeChannel.addObserver(mReservationStatusChanged);
            mReservationStatusSubscribeChannel.listen();
        }
        if (mControllerStatusSubscribeChannel == null) {
            mControllerStatusSubscribeChannel = NetworkConnectionManager.getInstance().createUpdateControllerStatusChannel();
            mControllerStatusSubscribeChannel.addObserver(mControllerStatusChanged);
            mControllerStatusSubscribeChannel.listen();
        }
    }

    private void unsubscribeEvents() {

        if (mReservationStatusSubscribeChannel != null) {
            mReservationStatusSubscribeChannel.removeObserver(mReservationStatusChanged);
            mReservationStatusSubscribeChannel.unlisten();
            mReservationStatusSubscribeChannel = null;
        }

        if (mControllerStatusSubscribeChannel != null) {
            mControllerStatusSubscribeChannel.unlisten();
            mControllerStatusSubscribeChannel.removeObserver(mControllerStatusChanged);
            mControllerStatusSubscribeChannel = null;
        }
    }

    private final IOnNotify mControllerStatusChanged = (networkChannel, uri, message) -> {

        String LOG_TAG = "mControllerStatusChanged";
        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            String topic = (String) uri.getLocation();
            StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

            if (topicTokenizer.countTokens() == 2) {

                try {

                    topicTokenizer.nextToken(); // skip "controller"
                    String physicalId = topicTokenizer.nextToken();

                    if (physicalId != null && physicalId.equals(SessionManager.getInstance().getControllerPhysicalId())) {

                        boolean available = true;
                        if (notificationMessage.getMessage().get("available") != null &&
                                !notificationMessage.getMessage().get("available").isNull()) {
                            available = notificationMessage.getMessage().get("available").asInt() == 1;
                        }

                        if (SessionManager.getInstance().isControllerAvailable() && !available) {
                            // TODO : If time is enough, we will change it to modeless Dialog later to show only 1 dialog.
                            new Thread(() -> {
                                JOptionPane.showMessageDialog(ScreenManager.getInstance().getCurrentScreen().getRootPanel(),
                                        Strings.CONTROLLER_ERROR + ":" + Strings.CONTACT_ATTENDANT,
                                        Strings.APPLICATION_NAME,
                                        JOptionPane.ERROR_MESSAGE);
                            }).start();
                        }

                        SessionManager.getInstance().setControllerAvailable(available);

                    } else {
                        Log.logv(LOG_TAG, "No such controller in session, physicalId=" + physicalId);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.logd(LOG_TAG, "Wrong topic name on ControllerStatusChangedChannel, topic=" + uri.getLocation());
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to parse notification message, exception occurred");
            e.printStackTrace();
        }
    };

    private final IOnNotify mReservationStatusChanged = (networkChannel, uri, message) -> {

        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {
            Log.logd(LOG_TAG, "mReservationStatusChanged Result=" + notificationMessage.getMessage() + " on topic=" + uri.getLocation());

            boolean expired = false;
            if (notificationMessage.getMessage().get("expired") != null &&
                    !notificationMessage.getMessage().get("expired").isNull()) {
                expired = notificationMessage.getMessage().get("expired").asInt() == 1;
            }

            boolean transactionStared = false;
            boolean transactionEnded = false;
            boolean isUnderTransaction = SessionManager.getInstance().getUnderTransaction();

            if (notificationMessage.getMessage().get("transaction") != null &&
                    !notificationMessage.getMessage().get("transaction").isNull()) {
                int transaction = notificationMessage.getMessage().get("transaction").asInt();

                if (transaction == 1 && !isUnderTransaction) transactionStared = true;
                if (transaction == 0 && isUnderTransaction) transactionEnded = true;
            }

            if (expired) {

                new Thread(() -> {
                    JOptionPane.showMessageDialog(ScreenManager.getInstance().getCurrentScreen().getRootPanel(),
                            Strings.GRACE_PERIOD_TIMEOUT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.INFORMATION_MESSAGE);
                }).start();

                mReservationStatusSubscribeChannel.unlisten();
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();

            } else if (transactionStared) {

                SessionManager.getInstance().setUnderTransaction(true);
                mBtnCancelReservation.setEnabled(false);

            } else if (transactionEnded) {

                final double fee = notificationMessage.getMessage().get("revenue").asDouble();

                new Thread(() -> {
                    JOptionPane.showMessageDialog(ScreenManager.getInstance().getCurrentScreen().getRootPanel(),
                            Strings.BYE_CUSTOMER + ", " + SessionManager.getInstance().getUserEmail() + ", you will be charged $" + fee,
                            Strings.APPLICATION_NAME,
                            JOptionPane.INFORMATION_MESSAGE);
                }).start();

                mReservationStatusSubscribeChannel.unlisten();
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to parse notification message, exception occurred");
            e.printStackTrace();
        }
    };

    private final ITaskDoneCallback mCancelReservationDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to cancel reservation due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.CANCEL_RESERVATION_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

        try {

            Log.logd(LOG_TAG, "Received response to CancelReservation, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                unsubscribeEvents();
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();

            } else if (success == 0) {

                Log.logd(LOG_TAG, "Failed to signup, with cause=" + resMsg.getMessage().get("cause").asString());

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.CANCEL_RESERVATION_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            } else {

                Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.CANCEL_RESERVATION_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to cancel reservation due to timeout");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.CANCEL_RESERVATION_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();
        }
    };
}
