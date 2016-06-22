package com.lge.notyet.driver.ui;

import com.lge.notyet.channels.ControllerStatusSubscribeChannel;
import com.lge.notyet.channels.ReservationStatusSubscribeChannel;
import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.LogoutTask;
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
import java.time.Instant;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ReservationHistoryPanel implements Screen {

    private static final String LOG_TAG = "ReservationHistoryPanel";

    private JLabel mLabelUserName;
    private JLabel mLabelReservationDate;
    private JLabel mLabelReservationLocation;
    private JLabel mLabelReservationConfirmationNumber;
    private JPanel mForm;
    private JButton mBtnCancelReservation;
    private JLabel mLabelLogout;
    private JLabel mLabelYouAre;

    private ReservationStatusSubscribeChannel mReservationStatusSubscribeChannel = null;
    private ControllerStatusSubscribeChannel mControllerStatusSubscribeChannel = null;

    @Override
    public void initScreen() {

        updateScreen();
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

    private void updateScreen() {

        SessionManager mSessionManager = SessionManager.getInstance();

        mLabelUserName.setText("Dear " + mSessionManager.getUserEmail());

        if (SessionManager.getInstance().getUnderTransaction()) {
            mLabelYouAre.setText("You're parked on");

            long parkedDurationMin = (Instant.now().getEpochSecond() - mSessionManager.getTransactionStartTimeStamp())/60;
            long parkedDurationSec = (Instant.now().getEpochSecond() - mSessionManager.getTransactionStartTimeStamp())%60;

            mLabelReservationDate.setText(parkedDurationMin + " min(s) " + parkedDurationSec + " sec(s)");
            startParkingTimeUpdateThread();

        } else {
            mLabelYouAre.setText("You're reserved on");

            Calendar reservedTime = Calendar.getInstance();
            reservedTime.setTimeInMillis(mSessionManager.getReservationTime()*1000);
            reservedTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MM/dd/yyyy z");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            String reservedTimeString = sdf.format(reservedTime.getTime());

            mLabelReservationDate.setText(reservedTimeString);
            stopParkingTimeUpdateThread();
        }

        int reservedFacilityId = mSessionManager.getReservationFacilityId();
        mLabelReservationLocation.setText("at " + mSessionManager.getFacilityName(reservedFacilityId));

        mLabelReservationConfirmationNumber.setText(mSessionManager.getReservationConfirmationNumber() + "");

        if (SessionManager.getInstance().getUnderTransaction()) {
            mBtnCancelReservation.setEnabled(false);
        } else {
            mBtnCancelReservation.setEnabled(true);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    public ReservationHistoryPanel() {

        // Log-out
        mLabelLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                doLogout();
            }
        });

        // Log-out
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogout();
                }
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

    private void doLogout() {
        unsubscribeEvents();
        TaskManager.getInstance().runTask(LogoutTask.getTask(SessionManager.getInstance().getKey(), null));
        SessionManager.getInstance().clear(); // Log-out
        ScreenManager.getInstance().showLoginScreen();
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
                                JOptionPane.showMessageDialog(getRootPanel(),
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
                    JOptionPane.showMessageDialog(getRootPanel(),
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

                long begin_ts = 0L;
                if (notificationMessage.getMessage().get("begin_ts") != null &&
                        !notificationMessage.getMessage().get("begin_ts").isNull()) {
                    begin_ts = notificationMessage.getMessage().get("begin_ts").asLong();
                } else {
                    begin_ts = Instant.now().getEpochSecond();
                }
                SessionManager.getInstance().setTransactionStartTimeStamp(begin_ts);
                startParkingTimeUpdateThread();

            } else if (transactionEnded) {

                final double fee = notificationMessage.getMessage().get("revenue").asDouble();

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.BYE_CUSTOMER + ", " + SessionManager.getInstance().getUserEmail() + ", you will be charged $" + fee,
                            Strings.APPLICATION_NAME,
                            JOptionPane.INFORMATION_MESSAGE);
                }).start();

                mReservationStatusSubscribeChannel.unlisten();
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();
                stopParkingTimeUpdateThread();
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

                final String failCause = resMsg.getMessage().get("cause").asString();
                Log.logd(LOG_TAG, "Failed to cancel reservation, with cause=" + failCause);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.CANCEL_RESERVATION_FAILED + ":" + failCause,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();

                if (failCause.equals(Strings.FAIL_CAUSE_INVALID_SESSION)) {
                    doLogout();
                }

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

    private ScheduledFuture<?> mParkingTimeUpdateThread = null;
    private static final int SLOT_STATUS_UPDATE_PERIOD = 1;
    private static final int SLOT_STATUS_UPDATE_MESSAGE_MAX = 3;
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(SLOT_STATUS_UPDATE_MESSAGE_MAX);

    private class SlotStatusUpdateThread implements Runnable {
        public void run() {
            updateScreen();
        }
    }

    private void startParkingTimeUpdateThread() {
        // TODO: Need to check Maximum Pended Requests?
        if (mParkingTimeUpdateThread == null) {
            mParkingTimeUpdateThread = mScheduler.scheduleAtFixedRate(new SlotStatusUpdateThread(), SLOT_STATUS_UPDATE_PERIOD, SLOT_STATUS_UPDATE_PERIOD, SECONDS);
        }
    }

    private void stopParkingTimeUpdateThread() {
        // TODO: Need to check Maximum Pended Requests?
        if (mParkingTimeUpdateThread != null) {
            mParkingTimeUpdateThread.cancel(true);
            mParkingTimeUpdateThread = null;
        }
    }
}
