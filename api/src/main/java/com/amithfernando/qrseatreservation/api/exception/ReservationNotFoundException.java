package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends ApiException {

    private static final String ERROR_CODE = "RESERVATION_NOT_FOUND";
    private static final String ERROR_MESSAGE = "Reservation %s not found";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public ReservationNotFoundException(String referenceNo) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(referenceNo));
    }
}
