package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.SellerControllerImpl;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/sellers")
@Tag(name = "Sellers", description = "Seller management API")
public interface SellerController {

    /**
     * Get all sellers
     *
     * @return List of all sellers
     */
    @Operation(summary = "Get all sellers", description = "Retrieves all registered sellers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sellers")
    })
    @GetMapping
    public ResponseEntity<List<SellerDetail>> getAllSellers();

    /**
     * Create a new seller
     *
     * @param sellerDetail Seller details
     * @return Success response
     */
    @Operation(summary = "Create seller", description = "Creates a new seller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seller created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid seller data")
    })
    @PostMapping
    public ResponseEntity<Void> createSeller(@RequestBody SellerDetail sellerDetail);

    /**
     * Create a new seller with individual parameters
     *
     * @param request Request containing seller creation details
     * @return Success response
     */
    @Operation(summary = "Create custom seller", description = "Creates a seller with individual parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seller created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid seller data")
    })
    @PostMapping("/custom")
    public ResponseEntity<Void> createCustomSeller(@RequestBody SellerControllerImpl.CreateSellerRequest request);

    /**
     * Delete a seller
     *
     * @param sellerDetail Seller to delete
     * @return Success response
     */
    @Operation(summary = "Delete seller", description = "Deletes a seller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Seller deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Seller not found")
    })
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
