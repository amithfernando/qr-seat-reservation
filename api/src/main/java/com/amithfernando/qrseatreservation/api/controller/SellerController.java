package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.CreateSellerRequest;
import com.amithfernando.qrseatreservation.api.dto.ErrorResponse;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import com.amithfernando.qrseatreservation.api.service.SellerDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
@Tag(name = "Sellers", description = "Seller management API")
@Slf4j
public class SellerController {

    private final SellerDetailService sellerDetailService;

    public SellerController(SellerDetailService sellerDetailService) {
        this.sellerDetailService = sellerDetailService;
    }

    /**
     * Get all sellers
     *
     * @return List of all sellers
     */
    @Operation(summary = "Get all sellers", description = "Retrieves all registered sellers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sellers"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<SellerDetail>> getAllSellers() {
        log.info("Fetching all sellers");
        List<SellerDetail> sellers = sellerDetailService.getAllSellers();
        return ResponseEntity.ok(sellers);
    }

    /**
     * Get a specific seller by ID
     *
     * @param id Seller ID
     * @return Seller details
     */
    @Operation(summary = "Get seller by ID", description = "Retrieves a specific seller by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved seller"),
            @ApiResponse(responseCode = "404", description = "Seller not found - SELLER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SellerDetail> getSellerById(@PathVariable Long id) {
        log.info("Fetching seller with id: {}", id);
        SellerDetail seller = sellerDetailService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    /**
     * Get a seller by name
     *
     * @param name Seller name
     * @return Seller details
     */
    @Operation(summary = "Get seller by name", description = "Retrieves a specific seller by their name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved seller"),
            @ApiResponse(responseCode = "404", description = "Seller not found - SELLER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<SellerDetail> getSellerByName(@RequestParam String name) {
        log.info("Fetching seller with name: {}", name);
        SellerDetail seller = sellerDetailService.getSellerByName(name);
        return ResponseEntity.ok(seller);
    }


    /**
     * Create a new seller with individual parameters
     *
     * @param request Request containing seller creation details
     * @return Success response
     */
    @Operation(summary = "Create custom seller", description = "Creates a seller with individual parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seller created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid seller data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Seller already exists - SELLER_ALREADY_EXISTS",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> createCustomSeller(@RequestBody CreateSellerRequest request) {
        log.info("Creating custom seller: {}", request.getName());
        sellerDetailService.createSeller(
                request.getName(),
                request.getAddress(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update an existing seller
     *
     * @param id Seller ID
     * @param request Updated seller details
     * @return Updated seller
     */
    @Operation(summary = "Update seller", description = "Updates an existing seller's details. Cannot update sellers with active reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seller updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid seller data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Seller not found - SELLER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<SellerDetail> updateSeller(@PathVariable Long id, @RequestBody CreateSellerRequest request) {
        log.info("Updating seller id: {}", id);
        SellerDetail updatedSeller = sellerDetailService.updateSeller(id, request);
        return ResponseEntity.ok(updatedSeller);
    }


    /**
     * Delete a seller
     *
     * @param id Seller to delete
     * @return Success response
     */
    @Operation(summary = "Delete seller", description = "Deletes a seller. Cannot delete sellers with active reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Seller deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Seller not found - SELLER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Seller has active reservations - SELLER_HAS_RESERVATIONS",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        log.info("Deleting seller: {}", id);
        sellerDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a seller can be modified (has no active reservations)
     *
     * @param id Seller ID to check
     * @return Boolean indicating if the seller can be modified
     */
    @Operation(summary = "Check if seller can be modified", description = "Checks if a seller has any active reservations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked seller status"),
            @ApiResponse(responseCode = "404", description = "Seller not found - SELLER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/can-modify")
    public ResponseEntity<Boolean> canModifySeller(@PathVariable Long id) {
        log.info("Checking if seller id {} can be modified", id);
        boolean canModify = sellerDetailService.canModifySeller(id);
        return ResponseEntity.ok(canModify);
    }
}
