package com.amithfernando.qrseatreservation.api.service;


import com.amithfernando.qrseatreservation.api.dto.CreateReservationRequest;
import com.amithfernando.qrseatreservation.api.dto.ReservationResponse;
import com.amithfernando.qrseatreservation.api.exception.ReservationNotFoundException;
import com.amithfernando.qrseatreservation.api.exception.SeatAlreadyReservedException;
import com.amithfernando.qrseatreservation.api.exception.SeatNotFoundException;
import com.amithfernando.qrseatreservation.api.exception.SellerNotFoundException;
import com.amithfernando.qrseatreservation.api.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.api.enums.SeatStatus;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import com.amithfernando.qrseatreservation.api.model.SeatDetail;
import com.amithfernando.qrseatreservation.api.model.SeatReservation;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import com.amithfernando.qrseatreservation.api.model.Ticket;
import com.amithfernando.qrseatreservation.api.repsitory.ReservationDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SeatReservationRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SellerDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ReservationService {

    private final ReservationDetailRepository reservationDetailRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatDetailRepository seatDetailRepository;
    private final SellerDetailRepository sellerDetailRepository;
    private final TicketService ticketService;

    public ReservationService(ReservationDetailRepository reservationDetailRepository, SeatReservationRepository seatReservationRepository, SeatDetailRepository seatDetailRepository, SellerDetailRepository sellerDetailRepository, TicketService ticketService) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.seatDetailRepository = seatDetailRepository;
        this.sellerDetailRepository = sellerDetailRepository;
        this.ticketService = ticketService;
    }

    @Transactional
    public ReservationDetail saveReservation(ReservationDetail reservationDetail) {
        //update ticket no
        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            log.info("Saving seat reservation: {}", seatReservation);
            SeatDetail seatDetail = seatDetailRepository.findById(seatReservation.getSeatDetail().getId()).get();
            String ticketNo = ticketService.getTicketNumber();
            seatReservation.setTicketNo(ticketNo);
            seatReservation.setSeatDetail(seatDetail);
            seatReservation.setReservationStatus(ReservationStatus.PAYMENT_PENDING);
            seatReservationRepository.save(seatReservation);
            //update seat status
            seatDetail.setSeatStatus(SeatStatus.RESERVED);
            seatDetailRepository.save(seatDetail);
        });
        reservationDetail.setReservationStatus(ReservationStatus.PAYMENT_PENDING);
        reservationDetail.setReferenceNo(UUID.randomUUID().toString());//small unique id
        ReservationDetail saved = reservationDetailRepository.save(reservationDetail);
        log.info("Reservation details created: {}", reservationDetail);
        return saved;
    }


    @Transactional
    public List<ReservationDetail> getAllReservations() {
        return reservationDetailRepository.findAllWithDetails();
    }

    @Transactional
    public void deleteReservation(Long id) {
        ReservationDetail reservationDetail = reservationDetailRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(id)));
        //update ticket no
        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            log.info("Deleting seat reservation: {}", seatReservation);
            SeatDetail seatDetail = seatDetailRepository.findById(seatReservation.getSeatDetail().getId()).get();
            seatReservationRepository.delete(seatReservation);
            //update seat status
            seatDetail.setSeatStatus(SeatStatus.AVAILABLE);
            seatDetailRepository.save(seatDetail);
        });
        reservationDetailRepository.delete(reservationDetail);
        log.info("Reservation details deleted: {}", reservationDetail);
    }

    public byte[] getTicketImageZip(ReservationDetail reservationDetail) throws IOException {
        if(reservationDetail==null){
            return null;
        }
        List<Ticket>   tickets=new ArrayList<>();
        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            Ticket byTicketNo = ticketService.findByTicketNo(seatReservation.getTicketNo());
            tickets.add(byTicketNo);
        });
        return ticketService.createZipFromImages(tickets);
    }

    @Transactional
    public void setPaymentDone(Long id) {
        ReservationDetail reservationDetail = reservationDetailRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(id)));
        //update ticket no
        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            log.info("Updating seat reservation to payment done: {}", seatReservation);
            SeatDetail seatDetail = seatDetailRepository.findById(seatReservation.getSeatDetail().getId()).get();
            seatReservation.setReservationStatus(ReservationStatus.PAID);
            seatReservationRepository.save(seatReservation);
        });
        reservationDetail.setReservationStatus(ReservationStatus.PAID);
        reservationDetailRepository.save(reservationDetail);
        log.info("Reservation details set payment done: {}", reservationDetail);
    }
    
    /**
     * Create a new reservation from DTO
     */
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // Validate seller exists
        SellerDetail seller = sellerDetailRepository.findById(request.getSellerId())
                .orElseThrow(() -> new SellerNotFoundException(String.valueOf(request.getSellerId())));

        // Validate and fetch seats
        Set<SeatReservation> seatReservations = new HashSet<>();
        for (CreateReservationRequest.SeatReservationItem item : request.getSeatReservations()) {
            SeatDetail seat = seatDetailRepository.findById(item.getSeatId())
                    .orElseThrow(() -> new SeatNotFoundException(String.valueOf(item.getSeatId())));

            // Check if seat is available
            if (!seat.isAvailable()) {
                throw new SeatAlreadyReservedException(seat.getSeatNo());
            }

            // Create seat reservation
            SeatReservation seatReservation = SeatReservation.builder()
                    .seatDetail(seat)
                    .ticketType(item.getTicketType())
                    .reservationStatus(ReservationStatus.PAYMENT_PENDING)
                    .build();
            seatReservations.add(seatReservation);
        }

        // Create reservation
        ReservationDetail reservation = ReservationDetail.builder()
                .sellerDetail(seller)
                .seatReservations(seatReservations)
                .description(request.getDescription())
                .reservationStatus(ReservationStatus.PAYMENT_PENDING)
                .build();

        // Save using existing method
        ReservationDetail saved = saveReservation(reservation);

        // Convert to response DTO
        return mapToReservationResponse(saved);
    }

    /**
     * Map ReservationDetail entity to ReservationResponse DTO
     */
    private ReservationResponse mapToReservationResponse(ReservationDetail reservation) {
        ReservationResponse.SellerInfo sellerInfo = ReservationResponse.SellerInfo.builder()
                .id(reservation.getSellerDetail().getId())
                .name(reservation.getSellerDetail().getName())
                .email(reservation.getSellerDetail().getEmail())
                .phone(reservation.getSellerDetail().getPhone())
                .build();

        List<ReservationResponse.SeatReservationInfo> seatInfoList = reservation.getSeatReservations().stream()
                .map(sr -> ReservationResponse.SeatReservationInfo.builder()
                        .seatId(sr.getSeatDetail().getId())
                        .seatNo(sr.getSeatDetail().getSeatNo())
                        .tableName(sr.getSeatDetail().getTableDetail().getTableName())
                        .ticketNo(sr.getTicketNo())
                        .ticketType(sr.getTicketType())
                        .build())
                .toList();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .referenceNo(reservation.getReferenceNo())
                .seller(sellerInfo)
                .seatReservations(seatInfoList)
                .description(reservation.getDescription())
                .status(reservation.getReservationStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * Get reservation by ID and return as DTO
     */
    public ReservationResponse getReservationById(Long id) {
        ReservationDetail reservation = reservationDetailRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(id)));
        return mapToReservationResponse(reservation);
    }

    /**
     * Get all reservations as DTOs
     */
    public List<ReservationResponse> getAllReservationsAsDto() {
        return getAllReservations().stream()
                .map(this::mapToReservationResponse)
                .toList();
    }
}
