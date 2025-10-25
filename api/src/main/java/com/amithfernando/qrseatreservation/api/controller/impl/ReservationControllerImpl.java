package com.amithfernando.qrseatreservation.api.controller.impl;

import com.amithfernando.qrseatreservation.api.controller.ReservationController;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import com.amithfernando.qrseatreservation.api.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
public class ReservationControllerImpl implements ReservationController {

    private final ReservationService reservationService;

    public ReservationControllerImpl(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public ResponseEntity<List<ReservationDetail>> getAllReservations() {
        log.info("Fetching all reservations");
        List<ReservationDetail> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    public ResponseEntity<ReservationDetail> findReservationByTicketNo(@PathVariable String ticketNo) {
        log.info("Finding reservation by ticket number: {}", ticketNo);
        ReservationDetail reservation = reservationService.findReservationByTicketNo(ticketNo);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation);
    }

    public ResponseEntity<Void> createReservation(@RequestBody ReservationDetail reservationDetail) {
        log.info("Creating new reservation for customer: {}", reservationDetail.getSellerDetail().getName());
        reservationService.saveReservation(reservationDetail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<Void> markPaymentDone(@RequestBody ReservationDetail reservationDetail) {
        log.info("Marking payment done for reservation: {}", reservationDetail.getReferenceNo());
        reservationService.setPaymentDone(reservationDetail);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<CheckInResponse> checkInByTicketNo(@PathVariable String ticketNo) {
        log.info("Checking in ticket: {}", ticketNo);
        boolean success = reservationService.checkInByTicketNo(ticketNo);
        if (success) {
            return ResponseEntity.ok(new CheckInResponse(true, "Ticket checked-in successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new CheckInResponse(false, "Ticket not found or already checked-in"));
        }
    }

    public ResponseEntity<byte[]> downloadTickets(@RequestBody ReservationDetail reservationDetail) {
        log.info("Downloading tickets for reservation: {}", reservationDetail.getReferenceNo());
        try {
            byte[] zipData = reservationService.getTicketImageZip(reservationDetail);
            if (zipData == null) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "tickets_" + reservationDetail.getReferenceNo() + ".zip");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);
        } catch (IOException e) {
            log.error("Error generating ticket ZIP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> deleteReservation(@RequestBody ReservationDetail reservationDetail) {
        log.info("Deleting reservation: {}", reservationDetail.getReferenceNo());
        reservationService.deleteReservation(reservationDetail);
        return ResponseEntity.noContent().build();
    }

}
