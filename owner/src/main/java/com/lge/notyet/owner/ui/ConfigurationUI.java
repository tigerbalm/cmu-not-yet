package com.lge.notyet.owner.ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.channels.GetFacilitiesResponseChannel;
import com.lge.notyet.channels.UpdateFacilityResponseChannel;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.business.GetFacilitiesTask;
import com.lge.notyet.owner.business.UpdateFacilityTask;
import com.lge.notyet.owner.manager.TaskManager;
import com.lge.notyet.owner.util.Log;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class ConfigurationUI extends JDialog {
    private static final String LOG_TAG = "Owner App";
    private JPanel contentPane;
    private JTextPane configureTextPane;
    private JPanel facilityList;
    private JButton updateButton;
    private JButton buttonOK;
    private ButtonGroup facilitySelected= new ButtonGroup();

    class FacilityData{
        public JTextField name;
        public JTextField fee;
        public JTextField fee_unit;
        public JTextField grace_period;
    }

    private HashMap<String, FacilityData> facilityControls= new HashMap<String, FacilityData>();

    public ConfigurationUI() {
        facilityList.setLayout(new GridLayout(0, 4, 4, 0));
        JPanel tempPanel2= new JPanel();
        tempPanel2.setLayout(new GridLayout(1, 0, 5, 0));
        tempPanel2.add(new JLabel(""));
        tempPanel2.add(new JLabel("Name"));
        tempPanel2.add(new JLabel(""));
        tempPanel2.add(new JLabel(""));
        tempPanel2.add(new JLabel(""));
        facilityList.add(tempPanel2);
        facilityList.add(new JLabel("Parking Fees (in dollars)"));
        facilityList.add(new JLabel("Unit time of parking (in seconds)"));
        facilityList.add(new JLabel("Grace Period (in seconds)"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();
        TaskManager.getInstance().runTask(GetFacilitiesTask.getTask(mGetFacilitiesResponseCallback));
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempFacilityID= facilitySelected.getSelection().getActionCommand();
                FacilityData tempFacilityData= facilityControls.get(tempFacilityID);
                String facilityName = tempFacilityData.name.getText();
                String facilityFee = tempFacilityData.fee.getText();
                String facilityFeeUnit = tempFacilityData.fee_unit.getText();
                String facilityGracePeriod = tempFacilityData.grace_period.getText();
                //Double dFacilityFee= Double.is //string.matches("[+-]?\\d*(\\.\\d+)?")

                if(facilityName.length()<30 && facilityName.length()>0 && facilityFee.length()>0 && facilityFeeUnit.length()>0 && facilityGracePeriod.length()>0
                        && facilityFee.matches("\\d*(\\.\\d+)?") && facilityFeeUnit.matches("\\d*") && facilityGracePeriod.matches("\\d*")) {
                    TaskManager.getInstance().runTask(UpdateFacilityTask.getTask(mUpdateFacilityResponseCallback, tempFacilityID, facilityName, facilityFee, facilityFeeUnit, facilityGracePeriod));
                }
                else{
                    JOptionPane.showMessageDialog(ConfigurationUI.this, "Check the input typed", "SurePark", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
    public static void main(String[] args){
        ConfigurationUI testUI= new ConfigurationUI();
        testUI.setVisible(true);
    }
    private ITaskDoneCallback mGetFacilitiesResponseCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to GetFacilitiesResponse due to timeout");
                JOptionPane.showMessageDialog(ConfigurationUI.this,
                        "Network Connection Error: Failed to GetFacilitiesResponse.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Received GetFacilitiesResponse response, message=" + resMsg.getMessage());

            try {

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success
                    JsonArray facilityArray= (JsonArray) resMsg.getMessage().get(GetFacilitiesResponseChannel.KEY_RESULT);
                    JPanel tempPanel;
                    JRadioButton tempRadioButton;
                    FacilityData tempFacilityData;
                    String tempFacilityID;
                    boolean firstValue=true;

//                    NumberFormat integerFormat = NumberFormat.getInstance();
//                    NumberFormatter integerFormatter = new NumberFormatter(integerFormat);
//                    integerFormatter.setValueClass(Integer.class);
//                    integerFormatter.setMinimum(0);
//                    integerFormatter.setMaximum(Integer.MAX_VALUE);
//                    integerFormatter.setAllowsInvalid(false);
//                    integerFormatter.setCommitsOnValidEdit(true);// If you want the value to be committed on each keystroke instead of focus lost
//
//                    DecimalFormat floatFormat = new DecimalFormat();//"####.##");
////                    NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
////                    floatFormatter.setValueClass(Double.class);
////                    floatFormatter.setMinimum(0);
////                    floatFormatter.setMaximum(Float.MAX_VALUE);
////                    floatFormatter.setAllowsInvalid(false);
////                    floatFormatter.setCommitsOnValidEdit(true);// If you want the value to be committed on each keystroke instead of focus lost
//                    floatFormat.setMaximumFractionDigits(2);
//                    floatFormat.setMaximumIntegerDigits(5);
//                    NumberFormatter floatFormatter= new NumberFormatter(floatFormat);
//                    floatFormatter.setAllowsInvalid(false);
//                    floatFormatter.setCommitsOnValidEdit(true);
//                    //JFormattedTextField field = new JFormattedTextField(integerFormatter);

                    for (JsonValue facilityItem:facilityArray) {
                        JsonObject facilityDetails= (JsonObject)facilityItem;
                        tempPanel= new JPanel();
                        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
                        tempRadioButton= new JRadioButton();
                        tempFacilityData= new FacilityData();
                        tempFacilityID= facilityDetails.get("id").toString();
                        facilityControls.put(tempFacilityID, tempFacilityData);

                        if (firstValue) {
                            firstValue= false;
                            tempRadioButton.setSelected(true);
                            updateButton.setEnabled(true);
                        }
                        facilitySelected.add(tempRadioButton);
                        tempRadioButton.setActionCommand(tempFacilityID);
                        tempPanel.add(tempRadioButton);
                        tempPanel.add(tempFacilityData.name= new JTextField(facilityDetails.get("name").asString()));
                        facilityList.add(tempPanel);

//                        tempFacilityData.fee= new JFormattedTextField(floatFormatter);
//                        ((JFormattedTextField)tempFacilityData.fee).setText(facilityDetails.get("fee").toString());//setValue(new Double(facilityDetails.get("fee").asDouble()));
//                        facilityList.add(tempFacilityData.fee);

//                        facilityList.add(tempFacilityData.fee= new JTextField(facilityDetails.get("fee").toString()));

//                        tempFacilityData.fee= new JFormattedTextField(floatFormatter);
//                        ((JFormattedTextField)tempFacilityData.fee).setText(facilityDetails.get("fee").toString());//setValue(new Double(facilityDetails.get("fee").asDouble()));
//                        facilityList.add(tempFacilityData.fee);
//
//
//                        tempFacilityData.fee_unit= new JFormattedTextField(integerFormatter);
//                        tempFacilityData.fee_unit.setText(facilityDetails.get("fee_unit").toString());
//                        facilityList.add(tempFacilityData.fee_unit);
//
//                        tempFacilityData.grace_period= new JFormattedTextField(integerFormatter);
//                        tempFacilityData.grace_period.setText(facilityDetails.get("grace_period").toString());
//                        facilityList.add(tempFacilityData.grace_period);
                        facilityList.add(tempFacilityData.fee= new JTextField(facilityDetails.get("fee").toString()));

                        facilityList.add(tempFacilityData.fee_unit= new JTextField(facilityDetails.get("fee_unit").toString()));

                        facilityList.add(tempFacilityData.grace_period= new JTextField(facilityDetails.get("grace_period").toString()));

                    }
                    facilityList.revalidate();
                    Log.log(LOG_TAG, "Success to GetFacilitiesResponse, resultSet is " + resMsg.getMessage().get(GetFacilitiesResponseChannel.KEY_RESULT).toString());

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to GetFacilitiesResponse, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(ConfigurationUI.this,
                            "Failed to GetFacilitiesResponse, fail cause=" + resMsg.getMessage().get("cause").asString(),
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private ITaskDoneCallback mUpdateFacilityResponseCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to UpdateFacilities due to timeout");
                JOptionPane.showMessageDialog(ConfigurationUI.this,
                        "Network Connection Error: Failed to UpdateFacilities.",
                        "SurePark",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            MqttNetworkMessage resMsg = (MqttNetworkMessage)response;
            Log.log(LOG_TAG, "Received UpdateFacilities response, message=" + resMsg.getMessage());

            try {

                int success = resMsg.getMessage().get("success").asInt();

                if (success == 1) { // Success
                    JOptionPane pane = new JOptionPane("Successfully updated", JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = pane.createDialog(ConfigurationUI.this, "Configuration Updation");
                    new Timer(2*1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    }).start();
                    dialog.setModal(true);
                    dialog.setVisible(true);

                    Log.log(LOG_TAG, "Success to UpdateFacilities");

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to UpdateFacilities, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(ConfigurationUI.this,
                            "Failed to UpdateFacilities, fail cause=" + resMsg.getMessage().get("cause").asString(),
                            "SurePark",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
