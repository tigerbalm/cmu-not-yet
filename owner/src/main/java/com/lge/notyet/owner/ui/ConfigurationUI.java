package com.lge.notyet.owner.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.channels.GetFacilitiesResponseChannel;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.business.StateMachine;
import com.lge.notyet.owner.business.getFacilitiesTask;
import com.lge.notyet.owner.manager.TaskManager;
import com.lge.notyet.owner.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigurationUI extends JDialog {
    private static final String LOG_TAG = "Owner App";
    private JPanel contentPane;
    private JTextPane configureTextPane;
    private JPanel facilityList;
    private JButton updateButton;
    private JButton buttonOK;

    public ConfigurationUI() {
        facilityList.setLayout(new GridLayout(0, 4));
        facilityList.add(new JLabel("Name"));
        facilityList.add(new JLabel("Parking Fees (in dollars)"));
        facilityList.add(new JLabel("Unit time of parking (in seconds)"));
        facilityList.add(new JLabel("Grace Period (in seconds)"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();
        TaskManager.getInstance().runTask(getFacilitiesTask.getTask(mFacilitiesListResponseCallback));
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
    public static void main(String[] args){
        ConfigurationUI testUI= new ConfigurationUI();
        testUI.setVisible(true);
    }
    private ITaskDoneCallback mFacilitiesListResponseCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to get Facilities List due to timeout");
                JOptionPane.showMessageDialog(ConfigurationUI.this,
                        "Network Connection Error: Failed to get FacilitiesList.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Received FacilitiesList response, message=" + resMsg.getMessage());

            try {

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success
                    JsonArray facilityArray= (JsonArray) resMsg.getMessage().get(GetFacilitiesResponseChannel.KEY_RESULT);
                    JPanel tempPanel;
                    JRadioButton tempRadioButton;
                    for (JsonValue facilityItem:facilityArray) {
                        JsonObject facilityDetails= (JsonObject)facilityItem;
                        tempPanel= new JPanel();
                        tempRadioButton= new JRadioButton();
                        tempRadioButton.setActionCommand(facilityDetails.get("id").toString());
                        tempPanel.add(tempRadioButton);
                        tempPanel.add(new JTextField(facilityDetails.get("name").toString()));
                        facilityList.add(tempPanel);
                        facilityList.add(new JTextField(facilityDetails.get("fee").toString()));
                        facilityList.add(new JTextField(facilityDetails.get("fee_unit").toString()));
                        facilityList.add(new JTextField(facilityDetails.get("grace_period").toString()));
                    }
                    facilityList.revalidate();
                    Log.log(LOG_TAG, "Success to FacilitiesList, resultSet is " + resMsg.getMessage().get(GetFacilitiesResponseChannel.KEY_RESULT).toString());

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to get Facilities List DB, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(ConfigurationUI.this,
                            "Failed to get Facilities List DB, fail cause=" + resMsg.getMessage().get("cause").asString(),
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
