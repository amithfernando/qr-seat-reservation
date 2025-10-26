package com.amithfernando.qrseatreservation.api.dto;

import lombok.Data;

@Data
public class CreateTableRequest {
    private String tableName;
    private int noOfAvailableSeats;
    private int noOfUnavailableSeats;
    private String description;
}
