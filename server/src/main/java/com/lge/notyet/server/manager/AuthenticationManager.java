package com.lge.notyet.server.manager;

import com.lge.notyet.server.proxy.DatabaseProxy;
import io.vertx.core.Future;

import java.util.UUID;

public class AuthenticationManager {
    private static AuthenticationManager instance = null;

    private DatabaseProxy databaseProxy;

    private AuthenticationManager() {
        databaseProxy = DatabaseProxy.getInstance(null);
    }

    public static AuthenticationManager getInstance() {
        synchronized (AuthenticationManager.class) {
            if (instance == null) {
                instance = new AuthenticationManager();
            }
            return instance;
        }
    }

    public Future<String> issueSession(String userId) {
        Future<String> future = Future.future();
        String sessionKey = UUID.randomUUID().toString();
        return future;
    }

    public String getSessionUser(String sessionKey) {
        return null;
    }

    public void invalidateSession(String sessionKey) {

    }
}
