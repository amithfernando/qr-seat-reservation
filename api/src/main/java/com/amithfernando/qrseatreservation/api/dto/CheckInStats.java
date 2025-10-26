package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Check-in statistics")
public  class CheckInStats {
    @Schema(description = "Total number of checked-in tickets")
    private Long totalCheckedIn;

    @Schema(description = "Total number of reserved (not checked-in) tickets")
    private Long totalReserved;
}
