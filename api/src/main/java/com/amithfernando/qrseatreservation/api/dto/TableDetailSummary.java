package com.amithfernando.qrseatreservation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableDetailSummary {

    private int totalNoOfTables;
    private int totalNoOfTotalSeats;

}
