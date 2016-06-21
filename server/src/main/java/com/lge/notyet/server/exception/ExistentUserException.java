package com.lge.notyet.server.exception;

public class ExistentUserException extends SureParkException {
    public ExistentUserException() {
        super("EXISTENT_USER");
    }
}
