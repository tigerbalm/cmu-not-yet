package com.lge.notyet.owner.ui;

import com.lge.notyet.owner.business.Query;
import com.lge.notyet.owner.business.StateMachine;

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
    private Specification_Result specialSettingAndResult;

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

        if(customAdditionalDeveloperQueryRadioButton.isSelected()==true)
            JOptionPane.showMessageDialog(this, "Custom option not implemented yet!!");
        else{
            StateMachine.getInstance().setQuery(choiceGroup.getSelection().getActionCommand(), customAdditionalDeveloperQueryRadioButton.isSelected());
            if(specialSettingAndResult==null) {
                specialSettingAndResult = new Specification_Result();
            }
            specialSettingAndResult.pack();
            specialSettingAndResult.setVisible(true);
            StateMachine.getInstance().setInternalState(StateMachine.States.MAINUI);
        }
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
        choiceGroup= new ButtonGroup();
        chooseReportPanel = new JPanel(new GridLayout(0,2));
        String defaultQuery= Query.getDefaultQueryId();
        for (String queryID: Query.getQueryIdList()) {
            String textToDisplay= Query.getDisplayString(queryID);
            JRadioButton jb= new JRadioButton(textToDisplay);
            jb.setActionCommand(queryID);
            if(defaultQuery.equalsIgnoreCase(queryID)){
                jb.setSelected(true);
            }
            chooseReportPanel.add(jb);
            choiceGroup.add(jb);
        }
        customAdditionalDeveloperQueryRadioButton= new JRadioButton("Custom (Additional developer query)");
        chooseReportPanel.add(customAdditionalDeveloperQueryRadioButton);

        revalidate();
        //FixMe: Move the functionality from Specification_Result file to this file.
        //FixMe: Add more settings option programmatically
        //FixMe: Update database to work without having sql_mode set to null.
        //FixMe: Add Slot condition for Query 3
        //FixMe: Do a formatted output of the report
        //FixMe: Make event handler for Ctrl+L key on main window for Log window to be visible.
    }
}
