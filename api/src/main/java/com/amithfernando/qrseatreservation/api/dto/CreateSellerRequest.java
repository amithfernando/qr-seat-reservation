package com.amithfernando.qrseatreservation.api.dto;

import lombok.Data;

@Data
public  class CreateSellerRequest {
    private String name;
    private String address;
    private String email;
    private String phoneNumber;
    private String description;
}
