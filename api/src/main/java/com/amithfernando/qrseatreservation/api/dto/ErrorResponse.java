package com.amithfernando.qrseatreservation.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Error code", example = "TABLE_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Error message", example = "Table T1 not found")
    private String message;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Timestamp of the error")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Request path where the error occurred", example = "/api/tables")
    private String path;

    @Schema(description = "Validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error details")
    public static class ValidationError {
        @Schema(description = "Field name", example = "tableName")
        private String field;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Error message", example = "must not be blank")
        private String message;
    }
}
