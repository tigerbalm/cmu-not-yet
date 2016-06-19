package com.lge.notyet.owner.test;

import javax.swing.*;

public class CompleteUI extends JDialog {
    private JPanel contentPane;
    private JPanel titlePane;
    private JPanel detailedContentPane;
    private JPanel logPane;
    private JTextPane textPane1;
    private JTextPane textPane2;
    private JTextPane reportTextPane;
    private JButton cofigureButton;
    private JButton buttonOK;

    public CompleteUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }

    public static void main(String[] args) {
        CompleteUI dialog = new CompleteUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        /*contentPane= new JPanel(){
            public Dimension getPreferredSize(){
                return new Dimension(800, 600);
            }
        } ;
        int panelWidth= 800-contentPane.getInsets().left*2;
        /*titlePane= new JPanel(null){
            public Dimension getPreferredSize(){
                return new Dimension(panelWidth, 60);
            }
        } ;
        detailedContentPane= new JPanel(null){
            public Dimension getPreferredSize(){
                return new Dimension(panelWidth, 540);
            }
        } ;*/
        /*textPane1= new JTextPane(){
            public Dimension getPreferredSize(){
                return new Dimension(panelWidth, 60);
            }
        };
        textPane2= new JTextPane(){
            public Dimension getPreferredSize(){
                return new Dimension(panelWidth, 540);
            }
        };*/
        /*logPane= new JPanel(){
            public Dimension getPreferredSize(){
                return new Dimension(panelWidth, 100);
            }
        } ;*/
    }
}
