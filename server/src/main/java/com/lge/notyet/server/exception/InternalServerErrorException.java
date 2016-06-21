package com.lge.notyet.server.exception;

public class InternalServerErrorException extends SureParkException {
    public InternalServerErrorException() {
        super("INTERNAL_SERVER_ERROR");
    }
}
