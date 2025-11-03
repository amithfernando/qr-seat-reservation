package com.amithfernando.qrseatreservation.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDetailResponse {

    private String name;
    private String address;
    private String email;
    private String phone;
    private String description;
}
