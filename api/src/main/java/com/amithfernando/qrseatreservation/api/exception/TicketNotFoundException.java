package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends ApiException {

    private static final String ERROR_CODE = "TICKET_NOT_FOUND";
    private static final String ERROR_MESSAGE = "Ticket %s not found";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public TicketNotFoundException(String ticketNo) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(ticketNo));
    }
}
