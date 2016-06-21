package com.lge.notyet.server.exception;

public class InvalidEmailPasswordException extends SureParkException {
    public InvalidEmailPasswordException() {
        super("INVALID_EMAIL_PASSWORD");
    }
}
