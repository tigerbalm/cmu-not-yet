package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.MakeReservationTask;
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

public class ReservationPanel implements Screen {

    private static final String LOG_TAG = "ReservationPanel";

    private JButton mBtnMakeReservation;
    private JPanel mForm;
    private JLabel mLabelUserName;
    private JComboBox mCbLocation;
    private JTextField mTfCreditCardNumber;
    private JSpinner mJSpinnerHour;
    private JLabel mLabelModifyAccountInfo;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    private long mRequestedTime = 0L;
    private int mRequestedFacilityId = 0;

    public ReservationPanel() {

        // Make a reservation
        mBtnMakeReservation.addActionListener(e -> {

            // Reserved Time
            mRequestedTime = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getModel().getDate().getTime()/1000;

            // Reserved Location
            String location = (String) mCbLocation.getSelectedItem();
            mRequestedFacilityId = SessionManager.getInstance().getFacilityId(location);

            TaskManager.getInstance().runTask(MakeReservationTask.getTask(mRequestedFacilityId, mRequestedTime, mReservationDoneCallback));

            setUserInputEnabled(false);
        });

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

        // Log out
        mLabelLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SessionManager.getInstance().clear();
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });

        // Log out
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                SessionManager.getInstance().clear();
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });
    }

    private final ITaskDoneCallback mReservationDoneCallback = (result, response) -> {

        setUserInputEnabled(true);

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to make reservation due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.MAKE_RESERVATION_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

        try {

            Log.logd(LOG_TAG, "Received response to MakeReservation, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                int confirmationNumber = resMsg.getMessage().get("confirmation_no").asInt();
                int reservationId = resMsg.getMessage().get("id").asInt();
                SessionManager.getInstance().setReservationInformation(mRequestedTime, confirmationNumber, mRequestedFacilityId, reservationId);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_DONE + confirmationNumber,
                        Strings.APPLICATION_NAME,
                        JOptionPane.PLAIN_MESSAGE);

                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (success == 0) {

                Log.logd(LOG_TAG, "Failed to make reservation, with cause=" + resMsg.getMessage().get("cause").asString());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            } else {

                Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.MAKE_RESERVATION_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to make reservation, exception occurred");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.MAKE_RESERVATION_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    };

    private void createUIComponents() {
        // TODO: place custom component creation code here
        final SpinnerDateModel model = new SpinnerDateModel();
        mJSpinnerHour = new JSpinner(model);
    }
}
