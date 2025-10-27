package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate tickets (uses settings if count not specified)")
public class GenerateTicketsRequest {

    @Schema(description = "Number of tickets to generate (optional, uses settings if not provided)", example = "100")
    @Min(value = 1, message = "Must generate at least 1 ticket")
    @Max(value = 10000, message = "Cannot generate more than 10000 tickets at once")
    private Integer count;
}
