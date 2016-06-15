package com.lge.notyet.owner.ui;

import com.lge.notyet.owner.business.StateMachine;

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
// add your code here
        //Find selected button
        String selectedQuery= choiceGroup.getSelection().getActionCommand();
        //choiceGroup.getSelection().getSelectedObjects()[0].toString();
        /*
        for (Enumeration<AbstractButton> buttons = choiceGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                selectedQuery= Integer.parseInt(button.getText());
                break;
            }
        }
        */
        if(customAdditionalDeveloperQueryRadioButton.isSelected()==true)
            JOptionPane.showMessageDialog(this, "Custom option not implemented yet!!");
        else{
            StateMachine.getInstance().setQuery(selectedQuery, customAdditionalDeveloperQueryRadioButton.isSelected());
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
        boolean firstControl= true;
        for (AbstractMap.SimpleEntry queryMapping:StateMachine.QUERY_LIST) {
            String textToDisplay= queryMapping.getKey().toString();
            JRadioButton jb= new JRadioButton(textToDisplay);
            jb.setActionCommand(textToDisplay);
            if(firstControl){
                firstControl= false;
                jb.setSelected(true);
            }
            choicePanel.add(jb);
            choiceGroup.add(jb);
        }
        customAdditionalDeveloperQueryRadioButton= new JRadioButton("Custom (Additional developer query)");
        choicePanel.add(customAdditionalDeveloperQueryRadioButton);
       // revalidate();
    }
}
