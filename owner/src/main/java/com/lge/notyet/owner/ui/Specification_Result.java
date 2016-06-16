package com.lge.notyet.owner.ui;

import com.eclipsesource.json.JsonValue;
import com.lge.notyet.channels.GetDBQueryResponseChannel;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.business.GenericTextResultHandler;
import com.lge.notyet.owner.business.StateMachine;
import com.lge.notyet.owner.business.dbQueryTask;
import com.lge.notyet.owner.manager.SessionManager;
import com.lge.notyet.owner.manager.TaskManager;
import com.lge.notyet.owner.util.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Specification_Result extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField startDate;
    private JTextField endDate;
    private JTextArea resultArea;
    private static final String LOG_TAG = "OwnerDBQueryPanel";


    public Specification_Result() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        resultArea.setText(StateMachine.getInstance().getQuery()+"\r\n");
        //dbQueryMysqlNetworking.getInstance().queryServer(resultArea, StateMachine.getInstance().getSqlQuery() );
        TaskManager.getInstance().runTask(dbQueryTask.getTask(StateMachine.getInstance().getSqlQuery(), mQueryResponseCallback));
    }

    private ITaskDoneCallback mQueryResponseCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to query due to timeout");
                JOptionPane.showMessageDialog(buttonOK,
                        "Network Connection Error: Failed to query.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Received query response, message=" + resMsg.getMessage());

            try {

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success
                    GenericTextResultHandler.handleResult(resultArea, resMsg.getMessage().get(GetDBQueryResponseChannel.KEY_RESULT));
                    
                    Log.log(LOG_TAG, "Success to query DB, resultSet is " + resMsg.getMessage().get(GetDBQueryResponseChannel.KEY_RESULT).toString());

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to query DB, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(buttonOK,
                            "Failed to query DB, fail cause=" + resMsg.getMessage().get("cause").asString(),
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

