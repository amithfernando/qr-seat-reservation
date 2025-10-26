package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class SellerHasReservationsException extends ApiException {

    private static final String ERROR_CODE = "SELLER_HAS_RESERVATIONS";
    private static final String ERROR_MESSAGE = "Cannot %s seller '%s' because they have active reservations";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public SellerHasReservationsException(String operation, String sellerName) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(operation, sellerName));
    }
}
