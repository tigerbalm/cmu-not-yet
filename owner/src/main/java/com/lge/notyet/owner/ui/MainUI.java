package com.lge.notyet.owner.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JRadioButton averageOccupancyRadioButton;
    private JRadioButton peakUsageHoursRadioButton;
    private JRadioButton howMuchTimeCarsRadioButton;
    private JRadioButton revenueBasedOnFacilityRadioButton;
    private JRadioButton customAdditionalDeveloperQueryRadioButton;
    private ButtonGroup choiceGroup;
    private Specification_Result specialSettingAndResult;

    public MainUI() {
        choiceGroup= new ButtonGroup();
        choiceGroup.add(averageOccupancyRadioButton);
        choiceGroup.add(peakUsageHoursRadioButton);
        choiceGroup.add(howMuchTimeCarsRadioButton);
        choiceGroup.add(revenueBasedOnFacilityRadioButton);
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
// add your code here
        if(customAdditionalDeveloperQueryRadioButton.isSelected()==true)
            JOptionPane.showMessageDialog(this, "Custom option not implemented yet!!");
        else{
            if(specialSettingAndResult==null) {
                specialSettingAndResult = new Specification_Result();
            }
            specialSettingAndResult.pack();
            specialSettingAndResult.setVisible(true);
        }
    }

    private void exitAll(){
        dispose();
        System.exit(0);
    }
    public static void main(String[] args) {
        MainUI dialog = new MainUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
