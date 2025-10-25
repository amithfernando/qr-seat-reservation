package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.ReservationControllerImpl;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
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
public interface ReservationController {

    /**
     * Get all reservations with details
     *
     * @return List of all reservations
     */
    @GetMapping
    public ResponseEntity<List<ReservationDetail>> getAllReservations();

    /**
     * Find reservation by ticket number
     *
     * @param ticketNo Ticket number
     * @return Reservation detail
     */
    @GetMapping("/ticket/{ticketNo}")
    public ResponseEntity<ReservationDetail> findReservationByTicketNo(@PathVariable String ticketNo);

    /**
     * Create a new reservation
     *
     * @param reservationDetail Reservation details
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<Void> createReservation(@RequestBody ReservationDetail reservationDetail);

    /**
     * Mark reservation payment as done
     *
     * @param reservationDetail Reservation to mark as paid
     * @return Success response
     */
    @PutMapping("/payment")
    public ResponseEntity<Void> markPaymentDone(@RequestBody ReservationDetail reservationDetail);

    /**
     * Check-in a ticket by ticket number
     *
     * @param ticketNo Ticket number to check-in
     * @return Success or error response
     */
    @PostMapping("/checkin/{ticketNo}")
    public ResponseEntity<ReservationControllerImpl.CheckInResponse> checkInByTicketNo(@PathVariable String ticketNo);

    /**
     * Download ticket images as ZIP file
     *
     * @param reservationDetail Reservation detail
     * @return ZIP file containing ticket images
     */
    @PostMapping("/tickets/download")
    public ResponseEntity<byte[]> downloadTickets(@RequestBody ReservationDetail reservationDetail);

    /**
     * Delete a reservation
     *
     * @param reservationDetail Reservation to delete
     * @return Success response
     */
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
