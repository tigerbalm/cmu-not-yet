package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.LogoutTask;
import com.lge.notyet.driver.business.MakeReservationTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public class ReservationPanel implements Screen {

    private static final String LOG_TAG = "ReservationPanel";

    private JButton mBtnMakeReservation;
    private JPanel mForm;
    private JLabel mLabelUserName;
    private JComboBox mCbLocation;
    private JTextField mTfCreditCardNumber;
    private JSpinner mJSpinnerHour;
    private JLabel mLabelLogout;

    private class FacilityComboBoxModel extends AbstractListModel implements ComboBoxModel {
        String mSelection = null;

        public Object getElementAt(int index) { return SessionManager.getInstance().getFacilityByIndex(index); }
        public int getSize() { return SessionManager.getInstance().getFacilitySize(); }
        public void setSelectedItem(Object anItem) { mSelection = (String) anItem; }
        public Object getSelectedItem() { return mSelection; }
    }

    @Override
    public void initScreen() {

        Calendar calNewYork = Calendar.getInstance();
        calNewYork.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        calNewYork.add(Calendar.MINUTE, +1);

        Calendar maxTime = Calendar.getInstance();
        maxTime.add(Calendar.HOUR_OF_DAY, 3);
        calNewYork.add(Calendar.MINUTE, 1);
        maxTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        Calendar minTime = Calendar.getInstance();
        minTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        SpinnerDateModel model = (SpinnerDateModel)mJSpinnerHour.getModel();
        SimpleDateFormat format = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getFormat();
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        model.setValue(calNewYork.getTime());
        model.setStart(minTime.getTime());
        model.setEnd(maxTime.getTime());

        mCbLocation.setModel(new FacilityComboBoxModel());
        if (SessionManager.getInstance().getFacilitySize() > 0) {
            mCbLocation.setSelectedIndex(0);
        }

        mLabelUserName.setText("Dear " + SessionManager.getInstance().getUserEmail());
        mTfCreditCardNumber.setText(SessionManager.getInstance().getCreditCardNumber());
    }

    @Override
    public void disposeScreen() {

    }

    @Override
    public JPanel getRootPanel() {
        return mForm;
    }

    @Override
    public String getName() {
        return "ReservationPanel";
    }

    private void setUserInputEnabled(boolean enabled) {
        mBtnMakeReservation.setEnabled(enabled);
        mJSpinnerHour.setEnabled(enabled);
        mCbLocation.setEnabled(enabled);
    }

    private void updateReservationTimeUI() {

        Calendar calNewYork = Calendar.getInstance();
        calNewYork.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        calNewYork.add(Calendar.MINUTE, 1);

        Calendar maxTime = Calendar.getInstance();
        maxTime.add(Calendar.HOUR_OF_DAY, 3);
        calNewYork.add(Calendar.MINUTE, 1);
        maxTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        Calendar minTime = Calendar.getInstance();
        minTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        SpinnerDateModel model = (SpinnerDateModel)mJSpinnerHour.getModel();
        SimpleDateFormat format = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getFormat();
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        model.setValue(calNewYork.getTime());
        model.setStart(minTime.getTime());
        model.setEnd(maxTime.getTime());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    public ReservationPanel() {

        // Make a reservation
        mBtnMakeReservation.addActionListener(e -> {

            // Reserved Time
            Log.logv(LOG_TAG, "" + Instant.now().toEpochMilli() + "/" + Instant.now().getEpochSecond());
            long requestedTime = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getModel().getDate().getTime()/1000;

            if (requestedTime < Instant.now().getEpochSecond()) {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_FAILED + ":" + Strings.TIME_ALREADY_PASSED,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                updateReservationTimeUI();
                return;
            }

            // Reserved Location
            String location = (String) mCbLocation.getSelectedItem();
            int requestedFacilityId = SessionManager.getInstance().getFacilityId(location);

            TaskManager.getInstance().runTask(MakeReservationTask.getTask(requestedFacilityId, requestedTime, mReservationDoneCallback));

            setUserInputEnabled(false);
        });

        // Log out
        mLabelLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                doLogout();
            }
        });

        // Log out
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogout();
                }
            }
        });
    }

    private void doLogout() {
        TaskManager.getInstance().runTask(LogoutTask.getTask(SessionManager.getInstance().getKey(), null));
        SessionManager.getInstance().clear(); // Log-out
        ScreenManager.getInstance().showLoginScreen();
    }

    private final ITaskDoneCallback mReservationDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to make reservation due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

        try {

            Log.logd(LOG_TAG, "Received response to MakeReservation, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                int reservationId = resMsg.getMessage().get("id").asInt();
                long reservationTime = resMsg.getMessage().get("reservation_ts").asLong();
                int confirmationNumber = resMsg.getMessage().get("confirmation_no").asInt();
                int facilityId = resMsg.getMessage().get("facility_id").asInt();
                String controllerPhysicalId = resMsg.getMessage().get("controller_physical_id").asString();

                SessionManager.getInstance().setReservationInformation(reservationTime, confirmationNumber, facilityId, reservationId, controllerPhysicalId);
                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (success == 0) {

                final String failCause = resMsg.getMessage().get("cause").asString();
                Log.logd(LOG_TAG, "Failed to make reservation, with cause=" + failCause);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.MAKE_RESERVATION_FAILED + ":" + failCause,
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();

                if (failCause.equals(Strings.FAIL_CAUSE_INVALID_SESSION)) {
                    doLogout();
                }

            } else {

                Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.MAKE_RESERVATION_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to make reservation, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();
        }
    };

    private void createUIComponents() {
        // TODO: place custom component creation code here
        final SpinnerDateModel model = new SpinnerDateModel();
        mJSpinnerHour = new JSpinner(model);
    }
}
