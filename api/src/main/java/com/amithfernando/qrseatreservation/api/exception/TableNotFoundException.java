package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class TableNotFoundException extends ApiException{

    private static final String ERROR_CODE = "TABLE_NOT_FOUND";
    private static final String ERROR_MESSAGE = "Table %s not found";
    private static final HttpStatus HTTP_STATUS =HttpStatus.NOT_FOUND;

    public TableNotFoundException(String identifier) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(identifier));
    }

}
