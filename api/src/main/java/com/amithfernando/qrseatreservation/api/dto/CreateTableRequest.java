package com.amithfernando.qrseatreservation.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateTableRequest {
    @NotEmpty(message = "Table Name cannot be empty")
    @Min(value = 2, message = "Table Name must be greater than 3 characters")
    @Max(value = 10, message = "Table Name must be less than 10 characters")
    private String tableName;
    @Min(value = 1, message = "No of seats must be greater than 0")
    @Max(value = 20, message = "No of seats must be less than 20")
    private int noOfAvailableSeats;
    @Min(value = 1, message = "No of seats must be greater than 0")
    @Max(value = 20, message = "No of seats must be less than 20")
    private int noOfUnavailableSeats;
    private String description;
}
