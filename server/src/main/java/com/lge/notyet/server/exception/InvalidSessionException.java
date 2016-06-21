package com.lge.notyet.server.exception;

public class InvalidSessionException extends SureParkException {
    public InvalidSessionException() {
        super("INVALID_SESSION");
    }
}
