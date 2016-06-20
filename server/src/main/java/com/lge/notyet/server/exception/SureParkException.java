package com.lge.notyet.server.exception;

public class SureParkException extends Exception {
    private SureParkException(String message) {
        super(message);
    }

    public static SureParkException createInternalServerErrorException() {
        return new SureParkException("INTERNAL_SERVER_ERROR");
    }

    public static SureParkException createNoAuthorizationException() {
        return new SureParkException("NO_AUTHORIZATION");
    }

    public static SureParkException createInvalidSessionException() {
        return new SureParkException("INVALID_SESSION");
    }

    public static SureParkException createExistentUserException() {
        return new SureParkException("EXISTENT_USER");
    }

    public static SureParkException createInvalidEmailPasswordException() {
        return new SureParkException("INVALID_EMAIL_PASSWORD");
    }

    public static SureParkException createInvalidConfirmationNumberException() {
        return new SureParkException("INVALID_CONFIRMATION_NO");
    }

    public static SureParkException createNoReservationExistException() {
        return new SureParkException("NO_RESERVATION_EXIST");
    }

    public static SureParkException createNoSlotExistException() {
        return new SureParkException("NO_SLOT_EXIST");
    }
}
