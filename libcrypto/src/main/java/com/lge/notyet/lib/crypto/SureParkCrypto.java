package com.lge.notyet.lib.crypto;

public class SureParkCrypto {
    private static final String SEED = "*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19";

    public static String encrypt(String seed, String cleartext) throws Exception {
        return Crypto.encrypt(seed, cleartext);
    }

    public static String decrypt(String seed, String encrypted) throws Exception {
        return Crypto.decrypt(seed, encrypted);
    }
}