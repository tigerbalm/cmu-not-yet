package com.lge.notyet.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.eclipse.paho.client.mqttv3.*;

public class MainVerticle extends AbstractVerticle {
    private MqttAsyncClient mqttAsyncClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        System.out.println("begin of start");

        try {
            mqttAsyncClient = new MqttAsyncClient("tcp://localhost", MqttAsyncClient.generateClientId());
            mqttAsyncClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("connectionLost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    String message = new String(mqttMessage.getPayload());
                    System.out.println("messageArrived: topic=" + topic + ", message=" + message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("deliveryComplete");
                }
            });
            mqttAsyncClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    System.out.println("connected to server");
                    try {
                        mqttAsyncClient.subscribe("#", 2);
                        startFuture.complete();
                    } catch (MqttException e) {
                        startFuture.fail(e);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    startFuture.fail(throwable);
                }
            });
        } catch (MqttException e) {
            startFuture.fail(e);
        }
    }

    @Override
    public void stop() throws Exception {
    }
}
