package com.amithfernando.qrseatreservation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public  class CheckInResponse {
    private boolean success;
    private String message;
}