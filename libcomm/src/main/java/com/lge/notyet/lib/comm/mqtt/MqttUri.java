package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-11.
 */

import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;

import java.util.StringTokenizer;

public class MqttUri extends Uri {

    private final static String ALL_TOPIC = "/#";

    public MqttUri(String path) {
        super(path);
    }

    public boolean isSuperOf(Uri arg) {

        if (arg == null) return false;

        if (arg instanceof MqttUri) {

            String topic1 = getPath();
            String topic2 = arg.getPath();

            if (topic1 == null || topic2 == null) return false;

            StringTokenizer topic1Tokenizer = new StringTokenizer(topic1, "/", true);
            StringTokenizer topic2Tokenizer = new StringTokenizer(topic2, "/", true);

            while(topic1Tokenizer.hasMoreTokens()) {

                if(!topic2Tokenizer.hasMoreTokens()) {
                    return false;
                }

                String token1 = topic1Tokenizer.nextToken();
                String token2 = topic2Tokenizer.nextToken();

                if(token1.equals("#")) return true;
                if(token1.equals("+")) continue;
                if (token1.equals(token2) == false) return false;
            }

            if (!topic2Tokenizer.hasMoreTokens()) return true;
        }

        return false;
    }
}
