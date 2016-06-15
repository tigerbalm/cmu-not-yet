package com.lge.notyet.owner.business;

/**
 * Created by gladvin.durai on 15-Jun-2016.
 */
public class Networking {
    private static Networking ourInstance = new Networking();

    public static Networking getInstance() {
        return ourInstance;
    }

    private Networking() {
    }

    public void queryServer(String query) {
    }
}
