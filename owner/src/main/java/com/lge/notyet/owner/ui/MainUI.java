package com.lge.notyet.owner.ui;

import com.lge.notyet.owner.business.Query;
import com.lge.notyet.owner.business.StateMachine;
import jdk.nashorn.internal.scripts.JD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.Enumeration;

public class MainUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JRadioButton customAdditionalDeveloperQueryRadioButton;
    private JPanel choicePanel;
    private JButton configureGracePeriodButton;
    private JTextPane reportTextPane;
    private JButton configureParkingFeeButton;
    private ButtonGroup choiceGroup;
    private Specification_Result specialSettingAndResult;

    public MainUI() {
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
        //Find selected button

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
        choicePanel= new JPanel(new GridLayout(0,1));
        String defaultQuery= Query.getDefaultQueryId();
        for (String queryID: Query.getQueryIdList()) {
            String textToDisplay= Query.getDisplayString(queryID);
            JRadioButton jb= new JRadioButton(textToDisplay);
            jb.setActionCommand(queryID);
            if(defaultQuery.equalsIgnoreCase(queryID)){
                jb.setSelected(true);
            }
            choicePanel.add(jb);
            choiceGroup.add(jb);
        }
        customAdditionalDeveloperQueryRadioButton= new JRadioButton("Custom (Additional developer query)");
        choicePanel.add(customAdditionalDeveloperQueryRadioButton);

        revalidate();
    }
}
