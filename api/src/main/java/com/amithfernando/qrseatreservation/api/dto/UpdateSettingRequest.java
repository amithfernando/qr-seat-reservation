package com.amithfernando.qrseatreservation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating application settings")
public class UpdateSettingRequest {

    @Schema(description = "Event name", example = "Annual Gala 2025")
    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String eventName;

    @Schema(description = "Venue location", example = "Grand Hall, City Center")
    @Size(max = 255, message = "Venue must not exceed 255 characters")
    private String venue;

    @Schema(description = "Table display size in pixels", example = "100")
    @Min(value = 10, message = "Table size must be at least 10")
    private Integer tableSize;

    @Schema(description = "Seat display size in pixels", example = "24")
    @Min(value = 4, message = "Seat size must be at least 4")
    private Integer seatSize;

    @Schema(description = "Number of columns in seating layout", example = "3")
    @Min(value = 1, message = "Number of columns must be at least 1")
    private Integer noOfColumns;

    @Schema(description = "Font size for ticket text", example = "14")
    @Min(value = 6, message = "Font size must be at least 6")
    private Integer fontSize;

    @Schema(description = "QR code X position on ticket", example = "50")
    @Min(value = 0, message = "QR X position must be non-negative")
    private Integer qrX;

    @Schema(description = "QR code Y position on ticket", example = "50")
    @Min(value = 0, message = "QR Y position must be non-negative")
    private Integer qrY;

    @Schema(description = "Text X position on ticket", example = "50")
    @Min(value = 0, message = "Text X position must be non-negative")
    private Integer textX;

    @Schema(description = "Text Y position on ticket", example = "120")
    @Min(value = 0, message = "Text Y position must be non-negative")
    private Integer textY;

    @Schema(description = "Ticket number prefix", example = "T-")
    @Size(max = 32, message = "Ticket prefix must not exceed 32 characters")
    private String ticketPrefix;

    @Schema(description = "Number of digits in ticket number", example = "5")
    @Min(value = 1, message = "Number of digits must be at least 1")
    private Integer noOfDigits;

    @Schema(description = "Maximum number of tickets to generate", example = "100")
    @Min(value = 0, message = "Max number of tickets must be non-negative")
    private Integer maxNoOfTickets;

    @Schema(description = "Base image for ticket generation (Base64 encoded)", example = "iVBORw0KGgoAAAANSUhEUgA...")
    private String baseImageBase64;
}
