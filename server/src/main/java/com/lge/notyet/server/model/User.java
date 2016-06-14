package com.lge.notyet.server.model;

import com.eclipsesource.json.JsonObject;

public class User {
    private int id;
    private int type;
    private String email;
    private String cardNumber;
    private String cardExpiration;

    public static final int USER_TYPE_OWNER = 0;
    public static final int USER_TYPE_ATTENDANT = 1;
    public static final int USER_TYPE_DRIVER = 2;

    public User(JsonObject userObject) {
        id = userObject.get("id").asInt();
        type = userObject.get("type").asInt();
        email = userObject.get("email").asString();
        cardNumber = userObject.get("cardNumber").asString();
        cardExpiration = userObject.get("cardExpiration").asString();
    }

    public boolean isOwner() {
        return type == USER_TYPE_OWNER;
    }

    public boolean isDriver() {
        return type == USER_TYPE_DRIVER;
    }

    public boolean isAttendant() {
        return type == USER_TYPE_ATTENDANT;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpiration() {
        return cardExpiration;
    }
}