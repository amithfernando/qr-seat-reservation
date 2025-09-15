package com.amithfernando.qrseatreservation.core.service;

import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.core.enums.SeatStatus;
import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.Ticket;
import com.amithfernando.qrseatreservation.core.repsitory.ReservationDetailRepository;
import com.amithfernando.qrseatreservation.core.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.core.repsitory.SeatReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// ... existing code ...
import com.amithfernando.qrseatreservation.core.model.SeatReservation;

@Service
@Slf4j
public class ReservationService {

    private final ReservationDetailRepository reservationDetailRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatDetailRepository seatDetailRepository;
    private final TicketService ticketService;

    public ReservationService(ReservationDetailRepository reservationDetailRepository, SeatReservationRepository seatReservationRepository, SeatDetailRepository seatDetailRepository, TicketService ticketService) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.seatDetailRepository = seatDetailRepository;
        this.ticketService = ticketService;
    }

    @Transactional
    public void saveReservation(ReservationDetail reservationDetail) {
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
        reservationDetailRepository.save(reservationDetail);
        log.info("Reservation details created: {}", reservationDetail);
    }


    @Transactional
    public List<ReservationDetail> getAllReservations() {
        return reservationDetailRepository.findAllWithDetails();
    }

    @Transactional
    public void deleteReservation(ReservationDetail reservationDetail) {
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
    public void setPaymentDone(ReservationDetail reservationDetail) {
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

    // Find the parent reservation by a seat ticket number
    @Transactional
    public ReservationDetail findReservationByTicketNo(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) return null;
        for (ReservationDetail rd : reservationDetailRepository.findAllWithDetails()) {
            if (rd.getSeatReservations() == null) continue;
            boolean match = rd.getSeatReservations().stream()
                    .anyMatch(sr -> sr != null && ticketNo.equals(sr.getTicketNo()));
            if (match) return rd;
        }
        return null;
    }

    // Mark a seat reservation as CHECKED_IN using the scanned ticket number
    @Transactional
    public boolean checkInByTicketNo(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) return false;
        SeatReservation target = null;
        for (ReservationDetail rd : reservationDetailRepository.findAllWithDetails()) {
            if (rd.getSeatReservations() == null) continue;
            for (SeatReservation sr : rd.getSeatReservations()) {
                if (sr != null && ticketNo.equals(sr.getTicketNo())) {
                    target = sr;
                    break;
                }
            }
            if (target != null) break;
        }
        if (target == null) {
            log.warn("No seat reservation found for ticket {}", ticketNo);
            return false;
        }
        if (target.getReservationStatus() == ReservationStatus.CHECKED_IN) {
            log.info("Ticket {} already checked-in", ticketNo);
            return false;
        }
        target.setReservationStatus(ReservationStatus.CHECKED_IN);
        seatReservationRepository.save(target);
        SeatDetail seatDetail = target.getSeatDetail();
        seatDetail.setSeatStatus(SeatStatus.CHECKED_IN);
        seatDetailRepository.save(seatDetail);
        log.info("Checked-in ticket {}", ticketNo);
        return true;
    }
}
