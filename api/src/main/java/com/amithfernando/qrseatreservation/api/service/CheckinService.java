package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.CheckInResponse;
import com.amithfernando.qrseatreservation.api.dto.CheckInStats;
import com.amithfernando.qrseatreservation.api.dto.TicketDetailsResponse;
import com.amithfernando.qrseatreservation.api.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.api.enums.SeatStatus;
import com.amithfernando.qrseatreservation.api.exception.TicketNotFoundException;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import com.amithfernando.qrseatreservation.api.model.SeatDetail;
import com.amithfernando.qrseatreservation.api.model.SeatReservation;
import com.amithfernando.qrseatreservation.api.repsitory.ReservationDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SeatReservationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CheckinService {

    private final ReservationDetailRepository reservationDetailRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatDetailRepository seatDetailRepository;

    public CheckinService(ReservationDetailRepository reservationDetailRepository,
                         SeatReservationRepository seatReservationRepository,
                         SeatDetailRepository seatDetailRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.seatDetailRepository = seatDetailRepository;
    }

    /**
     * Get ticket details by ticket number
     */
    public TicketDetailsResponse getTicketDetails(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) {
            throw new TicketNotFoundException(ticketNo);
        }

        // Find reservation and seat reservation
        ReservationDetail reservation = findReservationByTicketNo(ticketNo);
        if (reservation == null) {
            throw new TicketNotFoundException(ticketNo);
        }

        SeatReservation seatReservation = reservation.getSeatReservations().stream()
                .filter(sr -> sr != null && ticketNo.equals(sr.getTicketNo()))
                .findFirst()
                .orElseThrow(() -> new TicketNotFoundException(ticketNo));

        // Build response
        TicketDetailsResponse.SellerInfo sellerInfo = TicketDetailsResponse.SellerInfo.builder()
                .id(reservation.getSellerDetail().getId())
                .name(reservation.getSellerDetail().getName())
                .email(reservation.getSellerDetail().getEmail())
                .phone(reservation.getSellerDetail().getPhone())
                .build();

        SeatDetail seat = seatReservation.getSeatDetail();
        TicketDetailsResponse.SeatInfo seatInfo = TicketDetailsResponse.SeatInfo.builder()
                .id(seat.getId())
                .seatNo(seat.getSeatNo())
                .tableId(seat.getTableDetail().getId())
                .tableName(seat.getTableDetail().getTableName())
                .build();

        // Determine if check-in is allowed
        boolean canCheckIn = false;
        String checkInMessage = "";

        if (reservation.getReservationStatus() != ReservationStatus.PAID) {
            checkInMessage = "Check-in only allowed for PAID reservations. Current status: "
                    + reservation.getReservationStatus();
        } else if (seatReservation.getReservationStatus() == ReservationStatus.CHECKED_IN) {
            checkInMessage = "Ticket already checked in";
        } else {
            canCheckIn = true;
            checkInMessage = "Ready for check-in";
        }

        return TicketDetailsResponse.builder()
                .ticketNo(ticketNo)
                .ticketType(seatReservation.getTicketType())
                .reservationStatus(seatReservation.getReservationStatus())
                .canCheckIn(canCheckIn)
                .checkInMessage(checkInMessage)
                .seller(sellerInfo)
                .seat(seatInfo)
                .reservationReferenceNo(reservation.getReferenceNo())
                .reservationCreatedAt(reservation.getCreatedAt())
                .build();
    }

    /**
     * Check in a ticket
     */
    @Transactional
    public CheckInResponse checkInTicket(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) {
            return CheckInResponse.builder()
                    .success(false)
                    .ticketNo(ticketNo)
                    .message("Invalid ticket number")
                    .build();
        }

        // Find reservation and seat reservation
        ReservationDetail reservation = findReservationByTicketNo(ticketNo);
        if (reservation == null) {
            log.warn("Ticket not found: {}", ticketNo);
            return CheckInResponse.builder()
                    .success(false)
                    .ticketNo(ticketNo)
                    .message("Ticket not found")
                    .build();
        }

        SeatReservation seatReservation = reservation.getSeatReservations().stream()
                .filter(sr -> sr != null && ticketNo.equals(sr.getTicketNo()))
                .findFirst()
                .orElse(null);

        if (seatReservation == null) {
            log.warn("Seat reservation not found for ticket: {}", ticketNo);
            return CheckInResponse.builder()
                    .success(false)
                    .ticketNo(ticketNo)
                    .message("Seat reservation not found")
                    .build();
        }

        // Validate reservation is paid
        if (reservation.getReservationStatus() != ReservationStatus.PAID) {
            log.warn("Check-in attempted for non-PAID reservation. Ticket: {}, Status: {}",
                    ticketNo, reservation.getReservationStatus());
            return CheckInResponse.builder()
                    .success(false)
                    .ticketNo(ticketNo)
                    .message("Check-in only allowed for PAID reservations. Current status: "
                            + reservation.getReservationStatus())
                    .build();
        }

        // Check if already checked in
        if (seatReservation.getReservationStatus() == ReservationStatus.CHECKED_IN) {
            log.info("Ticket {} already checked in", ticketNo);
            return CheckInResponse.builder()
                    .success(false)
                    .ticketNo(ticketNo)
                    .message("Ticket already checked in")
                    .seatNo(seatReservation.getSeatDetail().getSeatNo())
                    .tableName(seatReservation.getSeatDetail().getTableDetail().getTableName())
                    .sellerName(reservation.getSellerDetail().getName())
                    .checkedInAt(seatReservation.getUpdatedAt())
                    .build();
        }

        // Perform check-in
        seatReservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        seatReservationRepository.save(seatReservation);

        SeatDetail seatDetail = seatReservation.getSeatDetail();
        seatDetail.setSeatStatus(SeatStatus.CHECKED_IN);
        seatDetailRepository.save(seatDetail);

        log.info("Successfully checked in ticket: {}", ticketNo);

        return CheckInResponse.builder()
                .success(true)
                .ticketNo(ticketNo)
                .message("Successfully checked in")
                .seatNo(seatDetail.getSeatNo())
                .tableName(seatDetail.getTableDetail().getTableName())
                .sellerName(reservation.getSellerDetail().getName())
                .checkedInAt(LocalDateTime.now())
                .build();
    }

    /**
     * Find reservation by ticket number
     */
    private ReservationDetail findReservationByTicketNo(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) {
            return null;
        }

        for (ReservationDetail rd : reservationDetailRepository.findAllWithDetails()) {
            if (rd.getSeatReservations() == null) continue;
            boolean match = rd.getSeatReservations().stream()
                    .anyMatch(sr -> sr != null && ticketNo.equals(sr.getTicketNo()));
            if (match) {
                return rd;
            }
        }
        return null;
    }

    /**
     * Get check-in statistics
     */
    public CheckInStats getCheckInStats() {
        long totalCheckedIn = reservationDetailRepository.findAllWithDetails().stream()
                .filter(r -> r.getSeatReservations() != null)
                .flatMap(r -> r.getSeatReservations().stream())
                .filter(sr -> sr != null && sr.getReservationStatus() == ReservationStatus.CHECKED_IN)
                .count();

        long totalReserved = reservationDetailRepository.findAllWithDetails().stream()
                .filter(r -> r.getSeatReservations() != null)
                .flatMap(r -> r.getSeatReservations().stream())
                .filter(sr -> sr != null && sr.getReservationStatus() != ReservationStatus.CHECKED_IN)
                .count();

        return new CheckInStats(totalCheckedIn, totalReserved);
    }


}
