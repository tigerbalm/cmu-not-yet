package com.lge.notyet.server.exception;

public class NoSlotExistException extends SureParkException {
    public NoSlotExistException() {
        super("NO_SLOT_EXIST");
    }
}
