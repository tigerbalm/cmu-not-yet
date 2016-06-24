package com.lge.notyet.owner.ui;

import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.business.LoginTask;
import com.lge.notyet.owner.manager.SessionManager;
import com.lge.notyet.owner.manager.TaskManager;
import com.lge.notyet.owner.util.Log;
import sun.awt.AppContext;

import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

    public class LoginPanel {

    private static final String LOG_TAG = "LoginPanel";

    private JTextField mTfUserEmailAddress;
    private JLabel mLabelForgetPassword;
    private JButton mBtnSignIn;
    private JPasswordField mTfUserPassword;
    private JPanel mForm;

    public static void main(String[] args){
        JDialog loginDialog = new JDialog();

        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);
        AppContext.getAppContext().put("JComponent.defaultLocale", locale);


        loginDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        loginDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        loginDialog.add(new LoginPanel().getRootPanel());
        loginDialog.pack();
        loginDialog.setModal(true);
        loginDialog.setVisible(true);
        MainUI.main(args);
    }
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
        mTfUserPassword.setText("");
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
                    SwingUtilities.getWindowAncestor(getRootPanel()).dispose();

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
