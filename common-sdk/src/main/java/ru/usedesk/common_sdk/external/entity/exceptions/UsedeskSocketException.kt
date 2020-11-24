package ru.usedesk.common_sdk.external.entity.exceptions;

import androidx.annotation.NonNull;

import static ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException.Error.UNKNOWN_ERROR;

public class UsedeskSocketException extends UsedeskException {
    private final Error error;

    public UsedeskSocketException() {
        this(UNKNOWN_ERROR);
    }

    public UsedeskSocketException(String message) {
        this(UNKNOWN_ERROR, message);
    }

    public UsedeskSocketException(@NonNull Error error) {
        this.error = error;
    }

    public UsedeskSocketException(@NonNull Error error, String message) {
        super(message);
        this.error = error;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + error.toString();
    }

    @NonNull
    public Error getError() {
        return error;
    }

    public enum Error {
        INTERNAL_SERVER_ERROR,
        BAD_REQUEST_ERROR,
        FORBIDDEN_ERROR,
        DISCONNECTED,
        IO_ERROR,
        JSON_ERROR,
        UNKNOWN_FROM_SERVER_ERROR,
        UNKNOWN_ERROR
    }
}
