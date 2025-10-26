package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after checking in a ticket")
public class CheckInResponse {

    @Schema(description = "Whether check-in was successful")
    private Boolean success;

    @Schema(description = "Ticket number that was checked in", example = "T-000001")
    private String ticketNo;

    @Schema(description = "Message describing the result", example = "Successfully checked in")
    private String message;

    @Schema(description = "Seat number", example = "S1")
    private String seatNo;

    @Schema(description = "Table name", example = "Table-A")
    private String tableName;

    @Schema(description = "Seller name", example = "John Doe")
    private String sellerName;

    @Schema(description = "Check-in timestamp")
    private LocalDateTime checkedInAt;
}