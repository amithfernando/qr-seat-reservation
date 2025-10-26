package com.amithfernando.qrseatreservation.api.dto;

import com.amithfernando.qrseatreservation.api.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.api.enums.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reservation response with all details")
public class ReservationResponse {

    @Schema(description = "Reservation ID", example = "1")
    private Long id;

    @Schema(description = "Unique reference number", example = "REF-20250001")
    private String referenceNo;

    @Schema(description = "Seller information")
    private SellerInfo seller;

    @Schema(description = "List of reserved seats")
    private List<SeatReservationInfo> seatReservations;

    @Schema(description = "Reservation description", example = "VIP guests")
    private String description;

    @Schema(description = "Reservation status", example = "PENDING")
    private ReservationStatus status;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Seller information")
    public static class SellerInfo {
        @Schema(description = "Seller ID", example = "1")
        private Long id;

        @Schema(description = "Seller name", example = "John Doe")
        private String name;

        @Schema(description = "Seller email", example = "john@example.com")
        private String email;

        @Schema(description = "Seller phone", example = "+1234567890")
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Seat reservation information")
    public static class SeatReservationInfo {
        @Schema(description = "Seat ID", example = "5")
        private Long seatId;

        @Schema(description = "Seat number", example = "S1")
        private String seatNo;

        @Schema(description = "Table name", example = "Table-A")
        private String tableName;

        @Schema(description = "Ticket number", example = "T-000001")
        private String ticketNo;

        @Schema(description = "Ticket type", example = "FULL")
        private TicketType ticketType;
    }
}
