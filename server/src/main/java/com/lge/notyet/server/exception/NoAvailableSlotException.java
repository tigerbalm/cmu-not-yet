package com.lge.notyet.server.exception;

public class NoAvailableSlotException extends SureParkException {
    public NoAvailableSlotException() {
        super("NO_AVAILABLE_SLOT");
    }
}
