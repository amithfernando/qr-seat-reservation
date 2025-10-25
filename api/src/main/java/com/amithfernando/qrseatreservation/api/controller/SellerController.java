package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.SellerControllerImpl;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/sellers")
public interface SellerController {

    /**
     * Get all sellers
     *
     * @return List of all sellers
     */
    @GetMapping
    public ResponseEntity<List<SellerDetail>> getAllSellers();

    /**
     * Create a new seller
     *
     * @param sellerDetail Seller details
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<Void> createSeller(@RequestBody SellerDetail sellerDetail);

    /**
     * Create a new seller with individual parameters
     *
     * @param request Request containing seller creation details
     * @return Success response
     */
    @PostMapping("/custom")
    public ResponseEntity<Void> createCustomSeller(@RequestBody SellerControllerImpl.CreateSellerRequest request);

    /**
     * Delete a seller
     *
     * @param sellerDetail Seller to delete
     * @return Success response
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteSeller(@RequestBody SellerDetail sellerDetail);

    /**
     * DTO for custom seller creation
     */
    @lombok.Data
    public static class CreateSellerRequest {
        private String name;
        private String address;
        private String email;
        private String phoneNumber;
        private String description;
    }
}
