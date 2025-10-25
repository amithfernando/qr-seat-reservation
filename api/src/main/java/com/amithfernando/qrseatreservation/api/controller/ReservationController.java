package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.ReservationControllerImpl;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation and ticket management API")
public interface ReservationController {

    /**
     * Get all reservations with details
     *
     * @return List of all reservations
     */
    @Operation(summary = "Get all reservations", description = "Retrieves all reservations with their details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations")
    })
    @GetMapping
    public ResponseEntity<List<ReservationDetail>> getAllReservations();

    /**
     * Find reservation by ticket number
     *
     * @param ticketNo Ticket number
     * @return Reservation detail
     */
    @Operation(summary = "Find reservation by ticket", description = "Finds a reservation using a ticket number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/ticket/{ticketNo}")
    public ResponseEntity<ReservationDetail> findReservationByTicketNo(@PathVariable String ticketNo);

    /**
     * Create a new reservation
     *
     * @param reservationDetail Reservation details
     * @return Success response
     */
    @Operation(summary = "Create reservation", description = "Creates a new seat reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data")
    })
    @PostMapping
    public ResponseEntity<Void> createReservation(@RequestBody ReservationDetail reservationDetail);

    /**
     * Mark reservation payment as done
     *
     * @param reservationDetail Reservation to mark as paid
     * @return Success response
     */
    @Operation(summary = "Mark payment done", description = "Updates a reservation to mark payment as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment marked as done"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data")
    })
    @PutMapping("/payment")
    public ResponseEntity<Void> markPaymentDone(@RequestBody ReservationDetail reservationDetail);

    /**
     * Check-in a ticket by ticket number
     *
     * @param ticketNo Ticket number to check-in
     * @return Success or error response
     */
    @Operation(summary = "Check-in ticket", description = "Marks a ticket as checked-in using its ticket number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket checked-in successfully"),
            @ApiResponse(responseCode = "400", description = "Ticket not found or already checked-in")
    })
    @PostMapping("/checkin/{ticketNo}")
    public ResponseEntity<ReservationControllerImpl.CheckInResponse> checkInByTicketNo(@PathVariable String ticketNo);

    /**
     * Download ticket images as ZIP file
     *
     * @param reservationDetail Reservation detail
     * @return ZIP file containing ticket images
     */
    @Operation(summary = "Download tickets", description = "Downloads all tickets for a reservation as a ZIP file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets downloaded successfully",
                    content = @Content(mediaType = "application/zip")),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Error generating tickets")
    })
    @PostMapping("/tickets/download")
    public ResponseEntity<byte[]> downloadTickets(@RequestBody ReservationDetail reservationDetail);

    /**
     * Delete a reservation
     *
     * @param reservationDetail Reservation to delete
     * @return Success response
     */
    @Operation(summary = "Delete reservation", description = "Deletes a reservation and frees up the seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteReservation(@RequestBody ReservationDetail reservationDetail);


    /**
     * DTO for check-in response
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CheckInResponse {
        private boolean success;
        private String message;
    }
}
