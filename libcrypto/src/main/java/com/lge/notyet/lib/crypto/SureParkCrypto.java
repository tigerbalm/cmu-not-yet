package com.lge.notyet.lib.crypto;

public class SureParkCrypto {
    private static final String SEED = "*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19";

    public static String encrypt(String cleartext) {
        try {
            return Crypto.encrypt(SEED, cleartext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encrypted) {
        try {
            return Crypto.decrypt(SEED, encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}