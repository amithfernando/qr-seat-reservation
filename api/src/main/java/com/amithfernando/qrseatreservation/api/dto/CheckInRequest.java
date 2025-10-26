package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for checking in a ticket")
public class CheckInRequest {

    @Schema(description = "Ticket number to check in", example = "T-000001", required = true)
    @NotBlank(message = "Ticket number is required")
    private String ticketNo;
}
