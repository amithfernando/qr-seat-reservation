package com.amithfernando.qrseatreservation.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private String ticketNumber;
    private String seatNumber;
}
