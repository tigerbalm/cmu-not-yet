package com.lge.notyet.attendant.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.attendant.business.GetFacilityTask;
import com.lge.notyet.attendant.business.GetSlotListTask;
import com.lge.notyet.attendant.business.ITaskDoneCallback;
import com.lge.notyet.attendant.business.LoginTask;
import com.lge.notyet.attendant.manager.ScreenManager;
import com.lge.notyet.attendant.manager.SessionManager;
import com.lge.notyet.attendant.manager.TaskManager;
import com.lge.notyet.attendant.resource.Strings;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;

public class LoginPanel implements Screen {

    private static final String LOG_TAG = "LoginPanel";

    private JTextField mTfUserEmailAddress;
    private JLabel mLabelForgetPassword;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JPanel mForm;

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

        // Forgot Password
        mLabelForgetPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Forgot Password
        mLabelForgetPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.INFORMATION_MESSAGE);
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

    private final ITaskDoneCallback mGetSlotListCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to get slot list due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.GET_SLOT_LIST_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);

            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to GetSlotListRequest, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                JsonArray slots = resMsg.getMessage().get("slots").asArray();

                for (JsonValue aSlot : slots.values()) {

                    JsonObject slot = aSlot.asObject();
                    int id = slot.get("id").asInt();  // Slot's Unique ID
                    int number = slot.get("number").asInt(); //
                    int occupied = slot.get("occupied").asInt();
                    int reserved = slot.get("reserved").asInt();
                    long occupied_ts = slot.get("occupied_ts").asLong();
                    // TODO: ADD this information
                    long reservation_ts = 0; // slot.get("reservation_ts").asLong();
                    // TODO: ADD this information
                    String user_email = ""; // slot.get("user_email").asString();
                    int controller_id = slot.get("controller_id").asInt();
                    // TODO: ADD this information
                    int physical_id = 1;//slot.get("physical_id").asInt();
                    SessionManager.getInstance().addSlot(id, number, occupied == 1, reserved==1, occupied_ts, controller_id, physical_id);
                }

                ScreenManager.getInstance().showFacilityMonitorScreen();

            } else if (success == 0) {
                Log.log(LOG_TAG, "Failed to get slot list, fail cause is " + resMsg.getMessage().get("cause").asString());
				
                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_SLOT_LIST_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);

                SessionManager.getInstance().clear();
            } else {

                Log.logd(LOG_TAG, "Failed to get slot list, unexpected result=" + success);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_SLOT_LIST_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
						
                SessionManager.getInstance().clear();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to get slot list, exception occurred");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.GET_SLOT_LIST_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);

            SessionManager.getInstance().clear();
        }
    };

    private final ITaskDoneCallback mGetFacilityCallback = (result, response) -> {

        if (result == ITaskDoneCallback.FAIL) {

            Log.logd(LOG_TAG, "Failed to get facility due to timeout");

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.GET_FACILITY_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                    Strings.APPLICATION_NAME,
                    JOptionPane.WARNING_MESSAGE);

            SessionManager.getInstance().clear();
            return;
        }

        MqttNetworkMessage resMsg = (MqttNetworkMessage)response;

        // Response may be wrong, we need to validate it, or handle exception
        try {

            Log.logd(LOG_TAG, "Received response to GetFacility, message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                SessionManager.getInstance().clearFacilityInformation();

                JsonArray facilities = resMsg.getMessage().get("facilities").asArray();

                if (facilities.size() != 1) {
                    Log.logd(LOG_TAG, "Wrong information from server, attendant should have only one facility");
                }

                //noinspection LoopStatementThatDoesntLoop
                for (JsonValue facility : facilities.values()) {
                    JsonObject fac = facility.asObject();
                    int id = fac.get("id").asInt();
                    String name = fac.get("name").asString();
                    SessionManager.getInstance().setFacilityInformation(id, name);
                    break;
                }

                TaskManager.getInstance().runTask(GetSlotListTask.getTask(
                        SessionManager.getInstance().getKey(),
                        SessionManager.getInstance().getFacilityId(),
                        mGetSlotListCallback));

            } else if (success == 0) {

                Log.logv(LOG_TAG, "Failed get facility, with cause=" + resMsg.getMessage().get("cause").asString());

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_FACILITY_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);

                SessionManager.getInstance().clear();

            } else {

                Log.logd(LOG_TAG, "Failed to get facility, unexpected result=" + success);

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.GET_FACILITY_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
						
				SessionManager.getInstance().clear();
            }

        } catch (Exception e) {

            Log.logd(LOG_TAG, "Failed to get facility, exception occurred");
            e.printStackTrace();

            JOptionPane.showMessageDialog(getRootPanel(),
                    Strings.GET_FACILITY_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                    Strings.APPLICATION_NAME,
                    JOptionPane.ERROR_MESSAGE);

            SessionManager.getInstance().clear();
        }
    };

    private final ITaskDoneCallback mLoginDoneCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            setUserInputEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {

                Log.logd(LOG_TAG, "Failed to login due to timeout");

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.LOGIN_FAILED + ":" + Strings.NETWORK_CONNECTION_ERROR,
                        Strings.APPLICATION_NAME,
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage) response;

            // Response may be wrong, we need to validate it, or handle exception
            try {

                Log.logd(LOG_TAG, "Received response to LoginRequest, message=" + resMsg.getMessage());

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success

                    // int userId = resMsg.getMessage().get("id").asInt(); // I will use SessionKey
                    int userType = resMsg.getMessage().get("type").asInt();
                    String session = resMsg.getMessage().get("session_key").asString();

                    if (userType != 1) { // Attendant

                        Log.logd(LOG_TAG, "This is not driver account");

                        JOptionPane.showMessageDialog(getRootPanel(),
                                Strings.LOGIN_FAILED + ":" + Strings.WRONG_ACCOUNT,
                                Strings.APPLICATION_NAME,
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (session == null) {

                        Log.logd(LOG_TAG, "Failed to validate response");

                        JOptionPane.showMessageDialog(getRootPanel(),
                                Strings.LOGIN_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                                Strings.APPLICATION_NAME,
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    SessionManager.getInstance().setUserEmail(mTfUserEmailAddress.getText());
                    SessionManager.getInstance().setKey(session);

                    // Retrieve Facilities
                    TaskManager.getInstance().runTask(GetFacilityTask.getTask(session, mGetFacilityCallback));

                } else if (success == 0) {

                    Log.logv(LOG_TAG, "Failed to login, with cause=" + resMsg.getMessage().get("cause").asString());

                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.LOGIN_FAILED + ":" + resMsg.getMessage().get("cause").asString(),
                            Strings.APPLICATION_NAME,
                            JOptionPane.WARNING_MESSAGE);

                } else {

                    Log.logd(LOG_TAG, "Failed to validate response, unexpected result=" + success);

                    JOptionPane.showMessageDialog(getRootPanel(),
                            Strings.LOGIN_FAILED + ":" + Strings.SERVER_ERROR + ", " + Strings.CONTACT_ATTENDANT,
                            Strings.APPLICATION_NAME,
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {

                Log.logd(LOG_TAG, "Failed to login, exception occurred");
                e.printStackTrace();

                JOptionPane.showMessageDialog(getRootPanel(),
                        Strings.LOGIN_FAILED + ":" + Strings.CONTACT_ATTENDANT,
                        Strings.APPLICATION_NAME,
                        JOptionPane.ERROR_MESSAGE);
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
