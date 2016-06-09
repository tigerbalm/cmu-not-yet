package com.lge.notyet.ui;

import org.eclipse.paho.client.mqttv3.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


class NetworkCommunicator implements Runnable{
    private static String serverURI;
    private static String uniqueClientID;
    private static MainDialog dialog;
    private static Thread backGroundThread= null;

    private static MqttClient client;
    private static MqttClient client2;

    private NetworkCommunicator(){
        print("Creating new thread...");
        backGroundThread= new Thread(this);
        backGroundThread.start();
    }

    //Singleton
    static void startService(String serverURI, String uniqueClientID, MainDialog dialog) {
        if(backGroundThread==null){
            NetworkCommunicator.serverURI= serverURI;
            NetworkCommunicator.uniqueClientID= uniqueClientID;
            NetworkCommunicator.dialog= dialog;
            new NetworkCommunicator();
        }
    }
    @Override
    public void run() {
        try {
            client = new MqttClient(serverURI, uniqueClientID+System.currentTimeMillis());
            client2 = new MqttClient(serverURI, uniqueClientID+"s"+System.currentTimeMillis());
            client2.setCallback(new MqttCallback(){
                public void connectionLost(Throwable cause){
                    print("Connection lost!");
                    print(Arrays.toString(cause.getStackTrace()));
                }
                public void deliveryComplete(IMqttDeliveryToken token){
                    try {
                        print("Pub complete" + new String(token.getMessage().getPayload()));
                    } catch (MqttException me) {
                        print(me);
                    }
                    print(token.toString());
                }

                private String slotPrefix= "/facilities/1/slots/";
                private String slotOccupiedMessage= "1"; 
                private String slotEmptyMessage= "0";

                public void messageArrived(String topic, MqttMessage message){
                    if(topic.toLowerCase().startsWith(slotPrefix.toLowerCase())){
                        int slotNumber= Integer.parseInt(topic.toLowerCase().substring(slotPrefix.length())) -1;
                        if(slotNumber>=0 && slotNumber<MainDialog.uiParkingSlot.length){
                            if(slotOccupiedMessage.equalsIgnoreCase(new String(message.getPayload())))
                                MainDialog.uiParkingSlot[slotNumber].setBackground(MainDialog.SLOT_OCCUPIED_COLOR);
                            else
                                MainDialog.uiParkingSlot[slotNumber].setBackground(MainDialog.SLOT_EMPTY_COLOR);

                        }
                    }
                    print(message.toString());
                    print(topic);
                }
            });
//            MqttConnectOptions mqttConnOption= new MqttConnectOptions();
//            mqttConnOption.setUserName("writeonly");
//            client.connect(mqttConnOption);
//
//            mqttConnOption= new MqttConnectOptions();
//            mqttConnOption.setUserName("readonly");
//            client2.connect(mqttConnOption);
            print("Connecting...");
            client.connect();

            client2.connect();
            print("Subscribing...");
            client2.subscribe("#");
        } catch (MqttException me) {
            print(me);
        }
    }

    static String sendMessageToBroker(String messageString, String topicId){
        String returnMessage=null;
        try {
            print("Publishing...");
            client.publish(topicId, new MqttMessage(messageString.getBytes()));
        } catch (MqttException me) {
            print(me);
        }
        return returnMessage;
    }


    public static void disconnect(){
        try {
            print("Disconnecting...");
            client.disconnect();
            client2.disconnect();
        }catch(MqttException me) {
            print(me);
        }

    }

    private static void print(MqttException me){
        print("reason "+me.getReasonCode());
        print("msg "+me.getMessage());
        print("loc "+me.getLocalizedMessage());
        print("cause "+me.getCause());
        print("excep "+me);

        print(Arrays.toString(me.getStackTrace()));

    }
    private static void print(String s) {
        dialog.printToLog(s);
    }

}
