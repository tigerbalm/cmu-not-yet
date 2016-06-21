package com.lge.notyet.server.exception;

public class NoReservationExistException extends SureParkException {
    public NoReservationExistException() {
        super("NO_RESERVATION_EXIST");
    }
}
