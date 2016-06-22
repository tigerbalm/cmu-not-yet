package com.lge.notyet.server.security;

import com.eclipsesource.json.JsonObject;

import java.util.EnumSet;
import java.util.Set;

public class Session {
    private String sessionKey;
    private int userId;
    private String cardNumber;
    private String cardExpiration;
    EnumSet<Privilege> privileges;

    public Session(String sessionKey, JsonObject userObject) {
        this.sessionKey = sessionKey;
        this.userId = userObject.get("user").asInt();
        this.cardNumber = userObject.get("card_number").isNull() ? null : userObject.get("card_number").asString();
        this.cardExpiration = userObject.get("card_expiration").isNull() ? null : userObject.get("card_expiration").asString();
        if (userObject.get("read_facility").asInt() == 1) addPrivileges(Privilege.READ_FACILITY);
        if (userObject.get("write_facility").asInt() == 1) addPrivileges(Privilege.WRITE_FACILITY);
        if (userObject.get("read_reservation").asInt() == 1) addPrivileges(Privilege.READ_RESERVATION);
        if (userObject.get("write_reservation").asInt() == 1) addPrivileges(Privilege.WRITE_RESERVATION);
        if (userObject.get("read_statistics").asInt() == 1) addPrivileges(Privilege.READ_STATISTICS);
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public int getUserId() {
        return userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpiration() {
        return cardExpiration;
    }

    public void addPrivileges(Privilege privilege) {
        privileges.add(privilege);
    }
}
