package com.amithfernando.qrseatreservation.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDetailResponse {

    private String tableName;
    private int totalSeats;
    private String description;
}
