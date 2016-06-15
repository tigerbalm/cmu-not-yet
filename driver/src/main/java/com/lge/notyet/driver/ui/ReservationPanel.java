package com.lge.notyet.driver.ui;

import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.driver.business.ReservationTask;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;
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
    private JLabel mLabelModifyAccountInfo;
    private JLabel mLabelLogout;

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationPanel";
    }

    class FacilityComboBoxModel extends AbstractListModel implements ComboBoxModel {

        String selection = null;

        public Object getElementAt(int index) {
            return SessionManager.getInstance().getFacilityByIndex(index);
        }

        public int getSize() {
            return SessionManager.getInstance().getFacilitySize();
        }

        public void setSelectedItem(Object anItem) {
            selection = (String) anItem; // to select and register an
        } // item from the pull-down list

        // Methods implemented from the interface ComboBoxModel
        public Object getSelectedItem() {
            return selection; // to add the selection to the combo box
        }
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

        mCbLocation.setModel(new FacilityComboBoxModel());
        if (SessionManager.getInstance().getFacilitySize() > 0) {
            mCbLocation.setSelectedIndex(0);
        }

        mLabelUserName.setText("Dear " + SessionManager.getInstance().getUserEmail());
        mTfCreditCardNumber.setText(SessionManager.getInstance().getCreditCardNumber());
    }

    private long mRequestedTime = 0L;
    private int mRequestedFacilityId = 0;

    public ReservationPanel() {

        mBtnMakeReservation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mRequestedTime = ((JSpinner.DateEditor) mJSpinnerHour.getEditor()).getModel().getDate().getTime()/1000;
                String location = (String) mCbLocation.getSelectedItem();
                mRequestedFacilityId = SessionManager.getInstance().getFacilityId(location);
                TaskManager.getInstance().runTask(ReservationTask.getTask(mRequestedFacilityId, mRequestedTime, mReservationDoneCallback));
                mBtnMakeReservation.setEnabled(false);
            }
        });
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
        mLabelLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SessionManager.getInstance().clear(); // Log-out
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
            }
        });
        mLabelLogout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                SessionManager.getInstance().clear(); // Log-out
                NetworkConnectionManager.getInstance().close();
                ScreenManager.getInstance().showLoginScreen();
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
            System.out.println("Success to make reservation, response message=" + resMsg.getMessage());

            if (resMsg.getResult() == 1) { // Success

                if(resMsg.validate() == false) {
                    System.out.println("Failed to validate response message");
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to validate response message",
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                System.out.println("Success to make reservation, confirmation number is " + resMsg.getConfirmationNumber());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Your reservation number is:" + resMsg.getConfirmationNumber(),
                        "SurePark",
                        JOptionPane.PLAIN_MESSAGE);

                int confirmationNumber = resMsg.getConfirmationNumber();
                int reservationId = resMsg.getReservationId();
                SessionManager.getInstance().setReservationInformation(mRequestedTime, confirmationNumber, mRequestedFacilityId, reservationId);

                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (resMsg.getResult() == 0) {
                System.out.println("Failed to make reservation, fail cause is " + resMsg.getFailCause());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to make reservation, fail cause=" + resMsg.getFailCause(),
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
