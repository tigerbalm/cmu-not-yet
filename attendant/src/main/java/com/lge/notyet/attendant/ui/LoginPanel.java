package com.lge.notyet.attendant.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.attendant.business.GetFacilityTask;
import com.lge.notyet.attendant.business.GetSlotListTask;
import com.lge.notyet.attendant.business.LoginTask;
import com.lge.notyet.attendant.manager.ScreenManager;
import com.lge.notyet.attendant.manager.SessionManager;
import com.lge.notyet.attendant.manager.TaskManager;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class LoginPanel {

    private static final String LOG_TAG = "LoginPanel";

    private JTextField mTfUserEmailAddress;
    private JLabel mLabelForgetPassword;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JPanel mForm;


    public LoginPanel() {

        // Log In
        mBtnSignIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                doLogin();
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

    public void init() {
        // mTfUserPassword.setText("");
    }

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

    private ITaskDoneCallback mGetSlotListCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to get slot list due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to get slot list.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Success to get slot list , response message=" + resMsg.getMessage());

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
                    int controller_id = slot.get("controller_id").asInt();
                    int physical_id = 0;//slot.get("physical_id").asInt();
                    SessionManager.getInstance().addSlot(id, number, occupied == 1, reserved==1, occupied_ts, controller_id, physical_id);
                }

                ScreenManager.getInstance().showFacilityMonitorScreen();

            } else if (success == 0) {
                Log.log(LOG_TAG, "Failed to get slot list, fail cause is " + resMsg.getMessage().get("cause").asString());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to get slot list, fail cause=" + resMsg.getMessage().get("cause").asString(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    private ITaskDoneCallback mGetFacilityCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to get facility information due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to get facility information.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Success to get facility information, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                JsonArray facilities = resMsg.getMessage().get("facilities").asArray();

                if (facilities.size() != 1) {
                    Log.log(LOG_TAG, "Wrong Information from server, attendant should have only one facility");
                }

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
                Log.log(LOG_TAG, "Failed to get facility information, fail cause is " + resMsg.getMessage().get("cause").asString());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to get facility information, fail cause=" + resMsg.getMessage().get("cause").asString(),
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

                    String session = resMsg.getMessage().get("session_key").asString();
                    SessionManager.getInstance().setUserEmail(mTfUserEmailAddress.getText());
                    SessionManager.getInstance().setKey(session);

                    Log.log(LOG_TAG, "Success to login, session key is " + resMsg.getMessage().get("session_key").asString());

                    TaskManager.getInstance().runTask(GetFacilityTask.getTask(session, mGetFacilityCallback));

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

}
