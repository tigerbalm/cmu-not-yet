package com.lge.notyet.driver.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.driver.business.CheckReservationTask;
import com.lge.notyet.driver.business.ITaskDoneCallback;
import com.lge.notyet.driver.business.LoginTask;
import com.lge.notyet.driver.business.UpdateFacilityListTask;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.driver.resource.Strings;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.crypto.SureParkCrypto;

import javax.swing.*;
import java.awt.event.*;


public class LoginPanel implements Screen {

    private static final String LOG_TAG = "LoginPanel";

    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JPasswordField mTfUserPassword;
    private JButton mBtnSignIn;
    private JLabel mLabelCreateAccount;
    private JLabel mLabelForgetPassword;

    public LoginPanel() {

        // Log In
        mBtnSignIn.addActionListener(e -> doLogin());

        // Log In
        mTfUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });

        // Sign Up
        mLabelCreateAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Sign Up Screen
                ScreenManager.getInstance().showSignUpScreen();
            }
        });

        // Sign Up
        mLabelCreateAccount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                // Sign Up Screen
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ScreenManager.getInstance().showSignUpScreen();
                }
            }
        });

        // Forgot Password
        mLabelForgetPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.INFORMATION_MESSAGE);
                }).start();
            }
        });

        // Forgot Password
        mLabelForgetPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.INFORMATION_MESSAGE);
                }).start();
            }
        });
    }

    @Override
    public void initScreen() {
        mTfUserPassword.setText("");
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
        return "LoginScreen";
    }

    private void setUserInputEnabled(boolean enabled) {
        mTfUserEmailAddress.setEnabled(enabled);
        mTfUserPassword.setEnabled(enabled);
        mBtnSignIn.setEnabled(enabled);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic

    private final ITaskDoneCallback mReservationCheckCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to check reservation due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_RESERVATION_INFO_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();
            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to GetReservation, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                int reservationId = resMsg.getMessage().get("id").asInt();
                long reservationTime = resMsg.getMessage().get("reservation_ts").asLong();
                int confirmationNumber = resMsg.getMessage().get("confirmation_no").asInt();
                int facilityId = resMsg.getMessage().get("facility_id").asInt();
                String controllerPhysicalId = resMsg.getMessage().get("controller_physical_id").asString();
                // String facilityName = resMsg.getMessage().get("facility_name").asString(); // We will retrieve it from SessionManager

                boolean transaction = resMsg.getMessage().get("begin_ts") != null &&
                        !resMsg.getMessage().get("begin_ts").isNull();

                if (transaction) {
                    SessionManager.getInstance().setUnderTransaction(true);
                    SessionManager.getInstance().setTransactionStartTimeStamp(resMsg.getMessage().get("begin_ts").asLong());
                }

                SessionManager.getInstance().setReservationInformation(reservationTime, confirmationNumber, facilityId, reservationId, controllerPhysicalId);
                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (success == 0) {

                Log.logd(LOG_TAG, "No reservation : " + resMsg.getMessage().get("cause").asString());
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();

            } else {

                Log.logd(LOG_TAG, "Failed to check reservation, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.GET_RESERVATION_INFO_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
                SessionManager.getInstance().clear();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to check reservation, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_RESERVATION_INFO_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();
            SessionManager.getInstance().clear();
        }
    };

    private final ITaskDoneCallback mUpdateFacilityListCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to update facility list due to timeout");

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.UPDATE_FACILITY_LIST_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
            }).start();
            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to UpdateFacilityList, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

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

                Log.logv(LOG_TAG, "Failed update facility list, with cause=" + resMsg.getMessage().get("cause").asString());

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.UPDATE_FACILITY_LIST_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();
                SessionManager.getInstance().clear();

            } else {

                Log.logd(LOG_TAG, "Failed to update facility list, unexpected result=" + success);

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.UPDATE_FACILITY_LIST_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
				SessionManager.getInstance().clear();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to update facility list, exception occurred");
            e.printStackTrace();

            new Thread(() -> {
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.UPDATE_FACILITY_LIST_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
            }).start();

            SessionManager.getInstance().clear();
        }
    };

    private final ITaskDoneCallback mLoginDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            setUserInputEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {

                Log.logd(LOG_TAG, "Failed to login due to timeout");

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.LOGIN_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);
                }).start();
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

            // Response may be wrong, we need to validate it, or handle exception
            try {

                Log.logd(LOG_TAG, "Received response to LoginRequest, message=" + resMsg.getMessage());

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success

                    // int userId = resMsg.getMessage().get("id").asInt(); // I will use SessionKey
                    String session = resMsg.getMessage().get("session_key").asString();
                    String creditCard = resMsg.getMessage().get("card_number").asString();
                    String cardExpiration = resMsg.getMessage().get("card_expiration").asString();

                    if (session == null || creditCard == null || cardExpiration == null) {

                        Log.logd(LOG_TAG, "Failed to validate response");

                        new Thread(() -> {
                            JOptionPane.showMessageDialog(getRootPanel(),
                                    Strings.LOGIN_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                                    Strings.APPLICATION_NAME,
                                    JOptionPane.ERROR_MESSAGE);
                        }).start();
                        return;
                    }

                    SessionManager.getInstance().setUserEmail(mTfUserEmailAddress.getText());
                    SessionManager.getInstance().setKey(session);

                    SessionManager.getInstance().setCreditCardNumber(SureParkCrypto.decrypt(creditCard));
                    SessionManager.getInstance().setCreditCardExpireDate(SureParkCrypto.decrypt(cardExpiration));

                    // Retrieve Facilities
                    TaskManager.getInstance().runTask(UpdateFacilityListTask.getTask(session, mUpdateFacilityListCallback));

                } else if (success == 0) {

                    Log.logv(LOG_TAG, "Failed to login, with cause=" + resMsg.getMessage().get("cause").asString());

                    new Thread(() -> {
                        JOptionPane.showMessageDialog(getRootPanel(),
                                Strings.LOGIN_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                                Strings.APPLICATION_NAME,
                                JOptionPane.WARNING_MESSAGE);
                    }).start();

                } else {

                    Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                    new Thread(() -> {
                        JOptionPane.showMessageDialog(getRootPanel(),
                                Strings.LOGIN_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                                Strings.APPLICATION_NAME,
                                JOptionPane.ERROR_MESSAGE);
                    }).start();
                }

            } catch (Exception e) {

                Log.logd(LOG_TAG, "Failed to login, exception occurred");
                e.printStackTrace();

                new Thread(() -> {
                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.LOGIN_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }).start();
            }
        }
    };

    private void doLogin() {

        if (SessionManager.getInstance().getKey() != null) {
            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.ALREADY_LOGIN,
                    Strings.APPLICATION_NAME,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Verify Inputs
        String userEmailAddress = mTfUserEmailAddress.getText();
        String userPassword = new String(mTfUserPassword.getPassword());

        Log.logv(LOG_TAG, "user email address=" + userEmailAddress + ", password=" + userPassword);

        if (userEmailAddress == null || userEmailAddress.length() == 0) {
            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.INPUT_EMAIL_ADDRESS,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userPassword.length() == 0) {
            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.INPUT_PASSWORD,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        setUserInputEnabled(false);
        TaskManager.getInstance().runTask(LoginTask.getTask(userEmailAddress, userPassword, mLoginDoneCallback));
    }
}
