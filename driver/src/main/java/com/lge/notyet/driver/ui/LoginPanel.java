package com.lge.notyet.driver.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.driver.business.CheckReservationTask;
import com.lge.notyet.driver.business.LoginTask;
import com.lge.notyet.driver.business.UpdateFacilityListTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;


public class LoginPanel {

    private static final String LOG_TAG = "LoginPanel";

    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JLabel mLabelCreateAccount;
    private JLabel mLabelForgetPassword;

    public LoginPanel() {

        // Log In
        mBtnSignIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                doLogin();
            }
        });

        mLabelCreateAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Sign Up Screen
                ScreenManager.getInstance().showSignUpScreen();
            }
        });

        mLabelCreateAccount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                // Sign Up Screen
                ScreenManager.getInstance().showSignUpScreen();
            }
        });
        mTfUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });
        mLabelForgetPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                JOptionPane.showMessageDialog(getRootPanel(),
                        "Please contact to operator/attendant, Telephone #: 111-222-3333",
                        "SurePark",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        mLabelForgetPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                JOptionPane.showMessageDialog(getRootPanel(),
                        "Please contact to operator/attendant, Telephone #: 111-222-3333",
                        "SurePark",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void doLogin() {
        // Verify Inputs
        String userEmailAddress = mTfUserEmailAddress.getText();
        String userPassword = new String(mTfUserPassword.getPassword());

        Log.logv(LOG_TAG, "user email address=" + userEmailAddress + ", password=" + userPassword);

        if (userEmailAddress == null || userEmailAddress.length() == 0) {
            JOptionPane.showMessageDialog(getRootPanel(),
                    "Please input user email address",
                    "SurePark",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userPassword.length() == 0) {
            JOptionPane.showMessageDialog(getRootPanel(),
                    "Please input user password",
                    "SurePark",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        setUserInputEnabled(false);
        TaskManager.getInstance().runTask(LoginTask.getTask(userEmailAddress, userPassword, mLoginDoneCallback));
    }

    private ITaskDoneCallback mReservationCheckCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to check reservation due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to check reservation information.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Success to check reservation information, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    Log.log(LOG_TAG, "Failed to validate response message");
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to validate response message",
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                */

                long reservationTime = resMsg.getMessage().get("reservation_ts").asLong();
                int confirmationNumber = resMsg.getMessage().get("confirmation_no").asInt();
                int facilityId = resMsg.getMessage().get("facility_id").asInt();
                int reservationId = resMsg.getMessage().get("id").asInt();

                SessionManager.getInstance().setReservationInformation(reservationTime, confirmationNumber, facilityId, reservationId);
                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (success == 0) {
                Log.log(LOG_TAG, "No Reservation : " + resMsg.getMessage().get("cause").asString());
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();
            }
        }
    };

    private ITaskDoneCallback mUpdateFacilityListCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to update facility list due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to update facility list.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Success to update facility list, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    Log.log(LOG_TAG, "Failed to validate response message");
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to validate response message",
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                */

                SessionManager.getInstance().clearFacility();
                JsonArray facilities = resMsg.getMessage().get("facilities").asArray();
                for (JsonValue facility : facilities.values()) {
                    JsonObject fac = facility.asObject();
                    int id = fac.get("id").asInt();
                    String name = fac.get("name").asString();
                    SessionManager.getInstance().addFacility(id, name);
                }

                TaskManager.getInstance().runTask(CheckReservationTask.getTask(SessionManager.getInstance().getKey(), mReservationCheckCallback));

            } else if (success == 0) {
                Log.log(LOG_TAG, "Failed update facility list, fail cause is " + resMsg.getMessage().get("cause").asString());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to update facility list, fail cause=" + resMsg.getMessage().get("cause").asString(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    private ITaskDoneCallback mLoginDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            setUserInputEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to login due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to login.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Received login response, message=" + resMsg.getMessage());

            try {

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    Log.log(LOG_TAG, "Failed to validate response message");
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to validate response message",
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                */

                    String session = resMsg.getMessage().get("session_key").asString();
                    String creditCard = resMsg.getMessage().get("card_number").asString();
                    String cardExpiration = resMsg.getMessage().get("card_expiration").asString();

                    SessionManager.getInstance().setUserEmail(mTfUserEmailAddress.getText());
                    SessionManager.getInstance().setKey(session);
                    SessionManager.getInstance().setCreditCardNumber(creditCard);
                    SessionManager.getInstance().setCreditCardExpireDate(cardExpiration);

                    Log.log(LOG_TAG, "Success to make reservation, session key is " + resMsg.getMessage().get("session_key").asString());

                    TaskManager.getInstance().runTask(UpdateFacilityListTask.getTask(session, mUpdateFacilityListCallback));

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to login, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to login, fail cause=" + resMsg.getMessage().get("cause").asString(),
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "LoginScreen";
    }

    private void setUserInputEnabled(boolean enabled) {
        mTfUserEmailAddress.setEnabled(enabled);
        mTfUserPassword.setEnabled(enabled);
        mBtnSignIn.setEnabled(enabled);
    }
}
