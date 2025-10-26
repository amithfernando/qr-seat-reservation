package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class TableAlreadyExistsException extends ApiException{

    private static String ERROR_CODE = "TABLE_ALREADY_EXISTS";
    private static String ERROR_MESSAGE = "Table %s already exists";
    private static HttpStatus HTTP_STATUS =HttpStatus.EXPECTATION_FAILED;

    public TableAlreadyExistsException(String tableName) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(tableName));
    }
}
