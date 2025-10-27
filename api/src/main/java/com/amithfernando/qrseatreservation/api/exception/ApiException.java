package com.amithfernando.qrseatreservation.api.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiException extends RuntimeException{

    private final String errorCode ;
    private  final HttpStatus httpStatus ;

    public ApiException(String errorCode, HttpStatus httpStatus,String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_SERVER_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
