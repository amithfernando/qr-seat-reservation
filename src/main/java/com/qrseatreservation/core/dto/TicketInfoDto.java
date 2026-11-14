package com.qrseatreservation.core.dto;

import com.qrseatreservation.core.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ticket information without BLOB data
 * Used for listing tickets without loading large binary data into memory
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoDto {
    private Long id;
    private String ticketNo;
    private TicketStatus status;
}
