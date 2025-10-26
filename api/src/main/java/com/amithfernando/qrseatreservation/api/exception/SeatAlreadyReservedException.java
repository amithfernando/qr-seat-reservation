package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class SeatAlreadyReservedException extends ApiException {

    private static final String ERROR_CODE = "SEAT_ALREADY_RESERVED";
    private static final String ERROR_MESSAGE = "Seat %s is already reserved";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public SeatAlreadyReservedException(String seatNo) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(seatNo));
    }
}
