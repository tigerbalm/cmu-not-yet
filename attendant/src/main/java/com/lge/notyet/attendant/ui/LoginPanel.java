package com.lge.notyet.attendant.ui;

import com.lge.notyet.attendant.business.LoginTask;
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

    /*
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


                if(resMsg.validate() == false) {
                    Log.log(LOG_TAG, "Failed to validate response message");
                    JOptionPane.showMessageDialog(getRootPanel(),
                            "Failed to validate response message",
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }


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
    */

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
                    SessionManager.getInstance().setUserEmail(mTfUserEmailAddress.getText());
                    SessionManager.getInstance().setKey(session);

                    Log.log(LOG_TAG, "Success to login, session key is " + resMsg.getMessage().get("session_key").asString());

                    // TaskManager.getInstance().runTask(UpdateFacilityListTask.getTask(session, mUpdateFacilityListCallback));

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
}
