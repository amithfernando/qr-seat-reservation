package com.amithfernando.qrseatreservation.api.exception;

import org.springframework.http.HttpStatus;

public class TicketAlreadyCheckedInException extends ApiException {

    private static final String ERROR_CODE = "TICKET_ALREADY_CHECKED_IN";
    private static final String ERROR_MESSAGE = "Ticket %s has already been checked in";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public TicketAlreadyCheckedInException(String ticketNo) {
        super(ERROR_CODE, HTTP_STATUS, ERROR_MESSAGE.formatted(ticketNo));
    }
}
