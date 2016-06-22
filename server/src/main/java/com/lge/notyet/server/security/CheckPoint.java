package com.lge.notyet.server.security;

public class CheckPoint {
    public static boolean authorize(Session session, Privilege... privileges) {
        for (Privilege privilege : privileges) {
            if (!session.privileges.contains(privilege)) return false;
        }
        return true;
    }
}
