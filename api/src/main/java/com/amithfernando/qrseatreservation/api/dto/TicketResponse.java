package com.amithfernando.qrseatreservation.api.dto;

import com.amithfernando.qrseatreservation.api.enums.TicketStatus;
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
@Schema(description = "Ticket information response")
public class TicketResponse {

    @Schema(description = "Ticket ID", example = "1")
    private Long id;

    @Schema(description = "Ticket number", example = "T-000001")
    private String ticketNo;

    @Schema(description = "Ticket status", example = "AVAILABLE")
    private TicketStatus status;

    @Schema(description = "Whether ticket has image data")
    private Boolean hasImage;

    @Schema(description = "Image data size in bytes", example = "15234")
    private Integer imageSizeBytes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
