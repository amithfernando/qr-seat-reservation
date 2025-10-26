package com.amithfernando.qrseatreservation.api.dto;

import com.amithfernando.qrseatreservation.api.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.api.enums.TicketType;
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
@Schema(description = "Detailed information about a ticket")
public class TicketDetailsResponse {

    @Schema(description = "Ticket number", example = "T-000001")
    private String ticketNo;

    @Schema(description = "Ticket type (FULL or HALF)", example = "FULL")
    private TicketType ticketType;

    @Schema(description = "Current reservation status", example = "PAID")
    private ReservationStatus reservationStatus;

    @Schema(description = "Whether ticket can be checked in")
    private Boolean canCheckIn;

    @Schema(description = "Reason if check-in is not allowed")
    private String checkInMessage;

    @Schema(description = "Seller information")
    private SellerInfo seller;

    @Schema(description = "Seat information")
    private SeatInfo seat;

    @Schema(description = "Reservation reference number", example = "REF-20250001")
    private String reservationReferenceNo;

    @Schema(description = "Reservation creation time")
    private LocalDateTime reservationCreatedAt;

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
    @Schema(description = "Seat information")
    public static class SeatInfo {
        @Schema(description = "Seat ID", example = "5")
        private Long id;

        @Schema(description = "Seat number", example = "S1")
        private String seatNo;

        @Schema(description = "Table ID", example = "1")
        private Long tableId;

        @Schema(description = "Table name", example = "Table-A")
        private String tableName;
    }
}
