
package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApiException {

    private static final String ERROR_CODE = "USER_ALREADY_EXISTS";
    private static final String ERROR_MESSAGE = "User '%s' already exists";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public UserAlreadyExistsException(String username) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(username));
    }
}
