package com.lge.notyet.ui;

import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import javax.net.ssl.*;
import java.security.cert.*;


class NetworkCommunicator implements Runnable{
    private static String serverAddress="si-gladvinc1"; //"192.168.43.24" //"localhost"
    private static final String portNumber= "8883"; //1883
    private static final boolean isSSLRequired= true;
    private static final String CertificateAuthorityPath= "D:\\programs\\JavaProg\\IdeaProjects\\cmu-not-yet\\mosquittoConfig\\certificates\\ca.crt";
    private static final boolean isAuthenticationRequired= true;
    private static final String userName= "server";
    private static final String password= "server";


    private static String uniqueClientID;
    private static MainDialog dialog;
    private static MqttClient client;
    private static MqttClient client2;

    private static Thread backGroundThread= null;
    private static final String protocol= "tcp://";
    private static final String secureProtocol= "ssl://";

    private NetworkCommunicator(){
        print("Creating new thread...");
        backGroundThread= new Thread(this);
        backGroundThread.start();
    }

    //Singleton
    static void startService(String uniqueClientID, MainDialog dialog) {
        if(backGroundThread==null){
            NetworkCommunicator.uniqueClientID= uniqueClientID;
            NetworkCommunicator.dialog= dialog;
            new NetworkCommunicator();
        }
    }
    @Override
    public void run() {
        try {
            String serverURI= null;
            if(isSSLRequired){
                serverURI= secureProtocol+serverAddress+":"+portNumber;
            }
            else{
                serverURI= protocol+serverAddress+":"+portNumber;
            }

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
            MqttConnectOptions mqttConnOption= new MqttConnectOptions();
            if(isAuthenticationRequired){
                mqttConnOption.setUserName(userName);
                mqttConnOption.setPassword(password.toCharArray());
            }

            if(isSSLRequired){
                try{
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca = cf.generateCertificate(new FileInputStream(CertificateAuthorityPath));
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(keyStore);
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                    mqttConnOption.setSocketFactory(sslContext.getSocketFactory());
                }catch(Exception sslConnectionError){
                    print(sslConnectionError);
                }
            }
            print("Connecting...");
            client.connect(mqttConnOption);

            MqttConnectOptions mqttConnOption2= new MqttConnectOptions();
            mqttConnOption2.setUserName("readonly");
            if(isAuthenticationRequired){
                mqttConnOption2.setUserName(userName);
                mqttConnOption2.setPassword(password.toCharArray());
            }

            if(isSSLRequired){
                try{
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca = cf.generateCertificate(new FileInputStream(CertificateAuthorityPath));
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(keyStore);
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                    mqttConnOption2.setSocketFactory(sslContext.getSocketFactory());
                }catch(Exception sslConnectionError){
                    print(sslConnectionError);
                }
            }
            print("Connecting...");
            client2.connect(mqttConnOption2);
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
    private static void print(Exception anyException){
        print("msg "+anyException.getMessage());
        print("loc "+anyException.getLocalizedMessage());
        print("cause "+anyException.getCause());
        print("excep "+anyException);

        print(Arrays.toString(anyException.getStackTrace()));

    }
    private static void print(String s) {
        dialog.printToLog(s);
    }

}
