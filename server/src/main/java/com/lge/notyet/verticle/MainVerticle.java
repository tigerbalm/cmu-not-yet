package com.lge.notyet.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.eclipse.paho.client.mqttv3.*;

import java.util.List;

public class MainVerticle extends AbstractVerticle {
    private MqttAsyncClient mqttAsyncClient;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getCanonicalName());
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        JsonObject mysqlConfig = new JsonObject().
                put("host", "127.0.0.1").
                put("username", "dba").
                put("password", "dba").
                put("database", "sure-park");
        AsyncSQLClient mysqlClient = MySQLClient.createShared(vertx, mysqlConfig, "MySQLPool1");
        mysqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.query("select id, occupied from controller", res1 -> {
                    if (res1.succeeded()) {
                        // Get the result set
                        ResultSet resultSet = res1.result();
                        for (String columnName : resultSet.getColumnNames()) {
                            System.out.println(columnName);
                        }

                        List<JsonArray> results = resultSet.getResults();

                        for (JsonArray row : results) {
                            int id = row.getInteger(0);
                            int occupied = row.getInteger(1);
                            System.out.println(id);
                            System.out.println(occupied);
                        }
                    } else {
                        startFuture.fail(res.cause());
                    }
                });
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        });

        /*
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
        */
    }
}
