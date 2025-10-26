package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class SellerNotFoundException extends ApiException {

    private static final String ERROR_CODE = "SELLER_NOT_FOUND";
    private static final String ERROR_MESSAGE = "Seller %s not found";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public SellerNotFoundException(String sellerName) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(sellerName));
    }
}
