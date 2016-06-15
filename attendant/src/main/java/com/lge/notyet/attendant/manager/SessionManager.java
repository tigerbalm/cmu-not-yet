package com.lge.notyet.attendant.manager;

import java.util.HashMap;

/**
 * Created by beney.kim on 2016-06-15.
 */
public class SessionManager {

    private final static String EMAIL = "email";
    private final static String KEY = "key";

    private HashMap<String, String> mSession;

    private static SessionManager sSessionManager = null;

    private SessionManager () {
        mSession = new HashMap<>();
    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (sSessionManager == null) {
                sSessionManager = new SessionManager();
            }
        }
        return sSessionManager;
    }

    public String getUserEmail() {
        return mSession.get(EMAIL);
    }

    public String getKey() {
        return mSession.get(KEY);
    }

    public void setUserEmail(String email) {
        mSession.put(EMAIL, email);
    }

    public void setKey(String key) {
        mSession.put(KEY, key);
    }

    public void clear() {
        mSession.clear();
    }
}
