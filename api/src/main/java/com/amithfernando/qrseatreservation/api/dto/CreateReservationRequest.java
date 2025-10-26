
package com.amithfernando.qrseatreservation.api.dto;

import com.amithfernando.qrseatreservation.api.enums.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new reservation")
public class CreateReservationRequest {

    @Schema(description = "ID of the seller making the reservation", example = "1", required = true)
    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @Schema(description = "List of seat reservations with their ticket types", required = true)
    @NotEmpty(message = "At least one seat reservation is required")
    @Valid
    private List<SeatReservationItem> seatReservations;

    @Schema(description = "Optional description or notes for the reservation", example = "VIP guests, special requirements")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual seat reservation with ticket type")
    public static class SeatReservationItem {

        @Schema(description = "ID of the seat to reserve", example = "5", required = true)
        @NotNull(message = "Seat ID is required")
        private Long seatId;

        @Schema(description = "Type of ticket (FULL or HALF)", example = "FULL", required = true)
        @NotNull(message = "Ticket type is required")
        private TicketType ticketType;
    }
}
