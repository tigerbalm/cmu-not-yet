package com.lge.notyet.kiosk.message;

import com.eclipsesource.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sjun.lee on 2016-06-22.
 */
public class MyMessageBuilder {
    String topic;
    String body;

    private MyMessageBuilder(String topic, String body) {
        this.topic = topic;
        this.body = body;
    }

    public String toSerialProtocol()
    {
        return "#" + topic + "##" + body + "#" + String.valueOf('\n');
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        String topic;
        //Map<String, String> items = new HashMap<>();
        JsonObject jsonObject = new JsonObject();

        public Builder() {

        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder append(String key, String value) {
            jsonObject.add(key, value);
            return this;
        }

        public MyMessageBuilder build() {
            return new MyMessageBuilder(topic, jsonObject.toString());
        }
    }
}
