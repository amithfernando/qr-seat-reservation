package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.CreateReservationRequest;
import com.amithfernando.qrseatreservation.api.dto.ErrorResponse;
import com.amithfernando.qrseatreservation.api.dto.ReservationResponse;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import com.amithfernando.qrseatreservation.api.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management API")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Get all reservations
     *
     * @return List of all reservations
     */
    @Operation(summary = "Get all reservations", description = "Retrieves all seat reservations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        log.info("Fetching all reservations");
        List<ReservationResponse> reservations = reservationService.getAllReservationsAsDto();
        return ResponseEntity.ok(reservations);
    }

    /**
     * Get a specific reservation by ID
     *
     * @param id Reservation ID
     * @return Reservation details
     */
    @Operation(summary = "Get reservation by ID", description = "Retrieves a specific reservation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found - RESERVATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        log.info("Fetching reservation with id: {}", id);
        ReservationResponse reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    /**
     * Create a new reservation
     *
     * @param request Reservation creation request
     * @return Created reservation details
     */
    @Operation(summary = "Create reservation", 
               description = "Creates a new seat reservation with specified seats and ticket types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Seller or seat not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Seat already reserved - SEAT_ALREADY_RESERVED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        log.info("Creating new reservation for seller ID: {}", request.getSellerId());
        ReservationResponse reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    /**
     * Delete a reservation
     *
     * @param id Reservation ID to delete
     * @return Success response
     */
    @Operation(summary = "Delete reservation", description = "Deletes a reservation and releases associated seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found - RESERVATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        log.info("Deleting reservation id: {}", id);
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark a reservation as paid
     *
     * @param id Reservation ID
     * @return Updated reservation
     */
    @Operation(summary = "Mark reservation as paid", description = "Updates reservation status to PAID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found - RESERVATION_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<ReservationResponse> markReservationAsPaid(@PathVariable Long id) {
        log.info("Marking reservation {} as paid", id);
        reservationService.setPaymentDone(id);
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok(response);
    }
}
