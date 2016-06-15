package com.lge.notyet.driver.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.driver.business.CheckReservationTask;
import com.lge.notyet.driver.business.LoginTask;
import com.lge.notyet.driver.business.UpdateFacilityListTask;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.ScreenManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.manager.TaskManager;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import sun.font.ScriptRun;

import javax.swing.*;
import java.awt.event.*;


public class LoginPanel {
    private JPanel mForm;
    private JTextField mTfUserEmailAddress;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JLabel mLabelCreateAccount;
    private JLabel mLabelForgetPassword;

    public LoginPanel() {
        mBtnSignIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                TaskManager.getInstance().runTask(LoginTask.getTask(mTfUserEmailAddress.getText(), mTfUserPassword.getPassword().toString(), mLoginDoneCallback));
                mTfUserEmailAddress.setEnabled(false);
                mTfUserPassword.setEnabled(false);
                mBtnSignIn.setEnabled(false);
            }
        });
        mLabelCreateAccount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ScreenManager.getInstance().showSignUpScreen();
            }
        });
        mLabelCreateAccount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                ScreenManager.getInstance().showSignUpScreen();
            }
        });
    }

    private ITaskDoneCallback mReservationCheckCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to check reservation due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to check reservation information.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            System.out.println("Success to check reservation information, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    System.out.println("Failed to validate response message");
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

                SessionManager.getInstance().setReservationInformation(reservationTime, confirmationNumber, facilityId);
                ScreenManager.getInstance().showReservationHistoryScreen();

            } else if (success == 0) {
                System.out.println("No Reservation : " + resMsg.getMessage().get("cause").asString());
                SessionManager.getInstance().clearReservationInformation();
                ScreenManager.getInstance().showReservationRequestScreen();
            }
        }
    };



    private ITaskDoneCallback mUpdateFacilityListCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to update facility list due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to update facility list.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            System.out.println("Success to update facility list, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    System.out.println("Failed to validate response message");
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
                System.out.println("Failed update facility list, fail cause is " + resMsg.getMessage().get("cause").asString());
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

            mTfUserEmailAddress.setEnabled(true);
            mTfUserPassword.setEnabled(true);
            mBtnSignIn.setEnabled(true);

            if (result == ITaskDoneCallback.FAIL) {
                System.out.println("Failed to make login due to timeout");
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Network Connection Error: Failed to login.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }


            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            System.out.println("Success to login, response message=" + resMsg.getMessage());

            int success = resMsg.getMessage().get("success").asInt();

            if (success == 1) { // Success

                /*
                if(resMsg.validate() == false) {
                    System.out.println("Failed to validate response message");
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

                SessionManager.getInstance().setKey(mTfUserEmailAddress.getText());
                SessionManager.getInstance().setKey(session);
                SessionManager.getInstance().setCreditCardNumber(creditCard);
                SessionManager.getInstance().setCreditCardExpireDate(cardExpiration);

                System.out.println("Success to make reservation, session key is " + resMsg.getMessage().get("session_key").asString());

                TaskManager.getInstance().runTask(UpdateFacilityListTask.getTask(session, mUpdateFacilityListCallback));

            } else if (success == 0) {
                System.out.println("Failed to login, fail cause is " + resMsg.getMessage().get("cause").asString());
                JOptionPane.showMessageDialog(getRootPanel(),
                        "Failed to login, fail cause=" + resMsg.getMessage().get("cause").asString(),
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "LoginScreen";
    }
}
