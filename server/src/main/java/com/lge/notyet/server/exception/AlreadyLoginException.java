package com.lge.notyet.server.exception;

public class AlreadyLoginException extends SureParkException {
    public AlreadyLoginException() {
        super("ALREADY_LOGIN");
    }
}
