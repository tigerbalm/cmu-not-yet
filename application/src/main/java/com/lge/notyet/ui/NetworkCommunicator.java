package com.lge.notyet.ui;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by gladvin.durai on 01-Jun-2016.
 */
public class NetworkCommunicator implements Runnable{
    private static String serverURI;
    private static String uniqueClientID;
    private static MainDialog dialog;
    private static Thread backGroundThread= null;

    private static MqttClient client;

    public NetworkCommunicator(){
        print("Creating new thread...");
        backGroundThread= new Thread(this);
        backGroundThread.start();
    }

    //Singleton
    public static void startService(String serverURI, String uniqueClientID, MainDialog dialog) {
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
            client = new MqttClient(serverURI, uniqueClientID);
            client.connect();
        } catch (MqttException me) {
            print(me);
        }
    }

    public static String sendMessageToBroker(String messageString, String topicId){
        String returnMessage=null;
        try {
            client.publish(topicId, new MqttMessage(messageString.getBytes()));
        } catch (MqttException me) {
            print(me);
        }
        return returnMessage;
    }


    public static void disconnect(){
        try {
            client.disconnect();
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

        print(me.getStackTrace().toString());
    }
    private static void print(String s) {
        dialog.printToLog(s);
    }

}
