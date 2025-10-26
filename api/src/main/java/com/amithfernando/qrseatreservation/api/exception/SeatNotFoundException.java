package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class SeatNotFoundException extends ApiException{

    private static final String ERROR_CODE = "SEAT_NOT_FOUND";
    private static final String ERROR_MESSAGE = "Seat %s not found";
    private static final HttpStatus HTTP_STATUS =HttpStatus.NOT_FOUND;

    public SeatNotFoundException(String identifier) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(identifier));
    }

}
