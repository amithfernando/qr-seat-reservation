package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException {

    private static final String ERROR_CODE = "INVALID_PASSWORD";
    private static final String ERROR_MESSAGE = "Invalid password provided";
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public InvalidPasswordException() {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE);
    }

    public InvalidPasswordException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
