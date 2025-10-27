package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";
    private static final String ERROR_MESSAGE = "User %s not found";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public UserNotFoundException(String userIdentifier) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(userIdentifier));
    }
}
