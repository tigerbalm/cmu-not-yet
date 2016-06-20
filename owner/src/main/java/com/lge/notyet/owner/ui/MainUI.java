package com.lge.notyet.owner.ui;

import com.lge.notyet.channels.GetDBQueryResponseChannel;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.business.GenericQueryHandler;
import com.lge.notyet.owner.business.Query;
import com.lge.notyet.owner.business.StateMachine;
import com.lge.notyet.owner.business.dbQueryTask;
import com.lge.notyet.owner.manager.TaskManager;
import com.lge.notyet.owner.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainUI extends JDialog {
    private JPanel contentPane;
    private JButton fetchReportButton;
    private JRadioButton customAdditionalDeveloperQueryRadioButton;
    private JPanel chooseReportPanel;
    private JButton configureGracePeriodButton;
    private JTextPane reportTextPane;
    private JButton configureParkingFeeButton;
    private JPanel chooseMoreSettingsPanel;
    private JTextPane textReportPane1;
    private JPanel graphicalPane;
    private JTextArea logArea;
    private JPanel logPanel;
    private ButtonGroup choiceGroup;
    //private Specification_Result specialSettingAndResult;

    public static final String LOG_TAG= "Owner App";

    public MainUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(fetchReportButton);

        fetchReportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFetchReport();
            }
        });
    }

    private void onFetchReport() {
//      JOptionPane.showMessageDialog(this, "Custom option not implemented yet!!");
        TaskManager.getInstance().runTask(dbQueryTask.getTask(StateMachine.getInstance().getQueryInstance().getSqlQuery(), mQueryResponseCallback));
    }

    private void exitAll(){
        dispose();
        System.exit(0);
    }
    public static void main(String[] args) {
        StateMachine.getInstance().setInternalState(StateMachine.States.MAINUI);

        MainUI dialog = new MainUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        ActionListener chooseReportHandler= new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readReportChoiceAndDoMoreSettings(e);
            }
            public void readReportChoiceAndDoMoreSettings(ActionEvent e){
                StateMachine.getInstance().setQuery(choiceGroup.getSelection().getActionCommand(), customAdditionalDeveloperQueryRadioButton.isSelected());
                if(e!=null) {//Not required for the first time initialization phase.
                    StateMachine.getInstance().getQueryInstance().fillMoreSettingPanel(chooseMoreSettingsPanel);
                }

            }
        };
        choiceGroup= new ButtonGroup();
        chooseReportPanel = new JPanel(new GridLayout(0,2));
        String defaultQuery= Query.getDefaultQueryId();
        for (String queryID: Query.getQueryIdList()) {
            String textToDisplay= Query.getInstance(queryID, false).getDisplayString();
            JRadioButton jb= new JRadioButton(textToDisplay);
            jb.setActionCommand(queryID);
            if(defaultQuery.equalsIgnoreCase(queryID)){
                jb.setSelected(true);
            }
            chooseReportPanel.add(jb);
            choiceGroup.add(jb);
            jb.addActionListener(chooseReportHandler);
        }
        customAdditionalDeveloperQueryRadioButton= new JRadioButton(Query.CUSTOM_QUERY);
        chooseReportPanel.add(customAdditionalDeveloperQueryRadioButton);
        customAdditionalDeveloperQueryRadioButton.addActionListener(chooseReportHandler);

        //Initiate the first action.
        chooseReportHandler.actionPerformed(null);

        revalidate();
        //FixMe: Configure grace period & fees
        //FixMe: Add more settings option programmatically
        //FixMe: Add sampling based on time for Query 1
        //FixMe: Update database to work without having sql_mode set to null. Query2
        //FixMe: Field names take from SQL response, instead of maintaining a redundant copy.
        //FixMe: Do a formatted output of the report
        //FixMe: Handle error conditions of server, by showing a popup
        //FixMe: Make event handler for Ctrl+L key on main window for Log window to be visible.
        //FixMe: Make dummy input values.->
            //  INSERT INTO `sure-park`.`reservation` (`id`, `user_id`, `slot_id`, `confirmation_no`, `reservation_ts`, `activated`, `fee`, `fee_unit`, `grace_period`) VALUES ('1', '1', '1', '1', '1', '1', '1', '1', '1');
            // INSERT INTO `sure-park`.`transaction` (`id`, `reservation_id`, `begin_ts`, `end_ts`, `revenue`) VALUES ('1', '1', '1466368729', '1466372329', '400');

        //FixMe: Add GUI output to the results
    }
    private ITaskDoneCallback mQueryResponseCallback = new ITaskDoneCallback() {

        @Override
        public void onDone(int result, Object response) {

            if (result == ITaskDoneCallback.FAIL) {
                Log.log(LOG_TAG, "Failed to query due to timeout");
                JOptionPane.showMessageDialog(textReportPane1,
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
                    StateMachine.getInstance().getQueryInstance().handleResult(textReportPane1, resMsg.getMessage().get(GetDBQueryResponseChannel.KEY_RESULT));

                    Log.log(LOG_TAG, "Success to query DB, resultSet is " + resMsg.getMessage().get(GetDBQueryResponseChannel.KEY_RESULT).toString());

                } else if (success == 0) {
                    Log.log(LOG_TAG, "Failed to query DB, fail cause is " + resMsg.getMessage().get("cause").asString());
                    JOptionPane.showMessageDialog(textReportPane1,
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
