package com.lge.notyet.server.exception;

public class NoAuthorizationException extends SureParkException {
    public NoAuthorizationException() {
        super("NO_AUTHORIZATION");
    }
}
