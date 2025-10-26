package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class SellerAlreadyExistsException extends ApiException{

    private static String ERROR_CODE = "SELLER_ALREADY_EXISTS";
    private static String ERROR_MESSAGE = "Seller %s already exists";
    private static HttpStatus HTTP_STATUS =HttpStatus.EXPECTATION_FAILED;

    public SellerAlreadyExistsException(String sellerName) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(sellerName));
    }
}
