package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class TableHasReservationsException extends ApiException {

    private static final String ERROR_CODE = "TABLE_HAS_RESERVATIONS";
    private static final String ERROR_MESSAGE = "Cannot %s table '%s' because it has active reservations";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public TableHasReservationsException(String operation, String tableName) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(operation, tableName));
    }
}
