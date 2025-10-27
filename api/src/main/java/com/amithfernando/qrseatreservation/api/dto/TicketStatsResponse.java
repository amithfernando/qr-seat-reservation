package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ticket statistics")
public class TicketStatsResponse {

    @Schema(description = "Total number of tickets")
    private Long total;

    @Schema(description = "Number of available tickets")
    private Long available;

    @Schema(description = "Number of used tickets")
    private Long used;

    @Schema(description = "Percentage of tickets used")
    private Double usagePercentage;
}
