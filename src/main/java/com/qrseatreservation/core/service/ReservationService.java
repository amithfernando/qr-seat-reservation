package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.model.SellerDetail;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.Ticket;
import com.qrseatreservation.core.model.TicketCategory;
import com.qrseatreservation.core.repository.ReservationDetailRepository;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.TicketCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// ... existing code ...
import com.qrseatreservation.core.model.SeatReservation;

@Service
@Slf4j
public class ReservationService {

    private final ReservationDetailRepository reservationDetailRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatDetailRepository seatDetailRepository;
    private final TicketService ticketService;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventContext eventContext;

    public ReservationService(ReservationDetailRepository reservationDetailRepository,
                              SeatReservationRepository seatReservationRepository,
                              SeatDetailRepository seatDetailRepository,
                              TicketService ticketService,
                              TicketCategoryRepository ticketCategoryRepository,
                              EventContext eventContext) {
        this.eventContext = eventContext;
        this.reservationDetailRepository = reservationDetailRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.seatDetailRepository = seatDetailRepository;
        this.ticketService = ticketService;
        this.ticketCategoryRepository = ticketCategoryRepository;
    }

    @Transactional
    public void saveReservation(ReservationDetail reservationDetail) {
        Event event = eventContext.requireEditable();
        Long eventId = event.getId();
        if (reservationDetail.getSellerDetail() != null) {
            EventContext.assertBelongsTo(reservationDetail.getSellerDetail().getEvent(), eventId);
        }
        // Build a price map: category name → TicketCategory (for price + FK)
        java.util.Map<String, TicketCategory> categoryMap = new java.util.HashMap<>();
        ticketCategoryRepository.findAllByEventOrdered(eventId).forEach(tc -> categoryMap.put(tc.getName(), tc));

        // Determine overall reservation status:
        // If ALL tickets are INVITATION, the whole reservation is PAID from the start.
        // Otherwise PAYMENT_PENDING (admin marks PAID after collecting cash).
        boolean allInvitation = reservationDetail.getSeatReservations().stream()
                .allMatch(sr -> sr.getTicketType() == TicketType.INVITATION);

        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            log.info("Saving seat reservation: {}", seatReservation);
            SeatDetail seatDetail = seatDetailRepository.findByIdAndEventId(seatReservation.getSeatDetail().getId(), eventId)
                    .orElseThrow(() -> new EventAccessException("Seat does not belong to the current event"));
            String ticketNo = ticketService.getTicketNumber(eventId);
            seatReservation.setTicketNo(ticketNo);
            seatReservation.setSeatDetail(seatDetail);

            // B: store price from ticket category
            TicketType type = seatReservation.getTicketType();
            String categoryName = type != null ? type.name() : "FULL";
            TicketCategory tc = categoryMap.get(categoryName);
            if (tc != null) {
                seatReservation.setTicketCategory(tc);
                seatReservation.setTicketPrice(tc.getPrice());
            } else {
                seatReservation.setTicketPrice(0.0);
            }

            // C: INVITATION tickets are immediately PAID (complimentary — no cash collected)
            if (type == TicketType.INVITATION) {
                seatReservation.setReservationStatus(ReservationStatus.PAID);
            } else {
                seatReservation.setReservationStatus(ReservationStatus.PAYMENT_PENDING);
            }

            seatReservationRepository.save(seatReservation);
            seatDetail.setSeatStatus(SeatStatus.RESERVED);
            seatDetailRepository.save(seatDetail);
        });

        reservationDetail.setReservationStatus(
                allInvitation ? ReservationStatus.PAID : ReservationStatus.PAYMENT_PENDING);
        reservationDetail.setReferenceNo(UUID.randomUUID().toString());
        reservationDetail.setEvent(event);
        reservationDetailRepository.save(reservationDetail);
        log.info("Reservation created: {}", reservationDetail);
    }


    @Transactional
    public List<ReservationDetail> getAllReservations() {
        return reservationDetailRepository.findAllWithDetails(eventContext.currentId());
    }

    /** Seller portal: only the seller's own reservations, only while their event is not archived. */
    @Transactional
    public List<ReservationDetail> getReservationsForSeller(SellerDetail seller) {
        if (seller == null || seller.getId() == null || seller.getEvent() == null) return List.of();
        List<ReservationDetail> list = reservationDetailRepository.findAllWithDetailsBySeller(seller.getEvent().getId(), seller.getId());
        list.forEach(rd -> EventContext.requireEditable(rd.getEvent()));
        return list;
    }

    @Transactional
    public void deleteReservation(ReservationDetail reservationDetail) {
        if (reservationDetail == null || reservationDetail.getId() == null) return;
        // Work on a managed copy: the UI holds a detached instance, and merging it after its
        // seat reservations are deleted fails. Unlink first so the join rows go before the seats.
        Long eventId = eventContext.requireEditable().getId();
        ReservationDetail managed = reservationDetailRepository.findByIdAndEventId(reservationDetail.getId(), eventId)
                .orElseThrow(() -> new IllegalStateException("Reservation no longer exists"));
        List<SeatReservation> seats = new ArrayList<>(
                managed.getSeatReservations() != null ? managed.getSeatReservations() : java.util.Set.of());
        if (managed.getSeatReservations() != null) managed.getSeatReservations().clear();
        reservationDetailRepository.delete(managed);
        reservationDetailRepository.flush();

        List<String> releasable = new ArrayList<>();
        for (SeatReservation seatReservation : seats) {
            log.info("Deleting seat reservation: {}", seatReservation);
            // A ticket someone already entered with is never resold; all others go back to the pool
            if (seatReservation.getTicketNo() != null && seatReservation.getReservationStatus() != ReservationStatus.CHECKED_IN) {
                releasable.add(seatReservation.getTicketNo());
            }
            SeatDetail seatDetail = seatReservation.getSeatDetail();
            seatReservationRepository.delete(seatReservation);
            if (seatDetail != null) {
                seatDetail.setSeatStatus(SeatStatus.AVAILABLE);
                seatDetailRepository.save(seatDetail);
            }
        }
        int released = ticketService.releaseTickets(eventId, releasable);
        log.info("Reservation deleted: {} ({} seats released, {} tickets back to available)",
                managed.getReferenceNo(), seats.size(), released);
    }

    public byte[] getTicketImageZip(ReservationDetail reservationDetail) throws IOException {
        if (reservationDetail == null) return null;
        return ticketService.createZipFromImages(renderTicketImages(reservationDetail));
    }

    /** Renders every ticket of the reservation with the current layout (same images as the ZIP download). */
    public List<Ticket> renderTicketImages(ReservationDetail reservationDetail) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        if (reservationDetail == null) return tickets;
        for (com.qrseatreservation.core.model.SeatReservation sr : reservationDetail.getSeatReservations()) {
            if (sr == null || sr.getTicketNo() == null) continue;
            // Always regenerate with current layout — stored blobs may be outdated
            String tableSeat = buildTableSeatLabel(sr);
            byte[] imgBytes = ticketService.generateOnTheFly(sr.getTicketNo(), tableSeat);
            tickets.add(new Ticket(sr.getTicketNo(),
                    com.qrseatreservation.core.enums.TicketStatus.AVAILABLE, imgBytes));
        }
        return tickets;
    }

    private String buildTableSeatLabel(com.qrseatreservation.core.model.SeatReservation sr) {
        if (sr.getSeatDetail() == null) return "";
        String table = sr.getSeatDetail().getTableDetail() != null
                ? sr.getSeatDetail().getTableDetail().getTableName() : "";
        String seat = sr.getSeatDetail().getSeatNo() != null
                ? sr.getSeatDetail().getSeatNo() : "";
        return (table + "  " + seat).trim();
    }

    /** Marks every unpaid ticket of the reservation as PAID. Checked-in tickets are left as they are. */
    @Transactional
    public void setPaymentDone(ReservationDetail reservationDetail) {
        EventContext.assertBelongsTo(reservationDetail.getEvent(), eventContext.requireEditable().getId());
        reservationDetail.getSeatReservations().forEach(seatReservation -> {
            if (seatReservation.getReservationStatus() == ReservationStatus.CHECKED_IN) return;
            log.info("Updating seat reservation to payment done: {}", seatReservation);
            seatReservation.setReservationStatus(ReservationStatus.PAID);
            seatReservationRepository.save(seatReservation);
        });
        reservationDetail.setReservationStatus(ReservationStatus.PAID);
        reservationDetailRepository.save(reservationDetail);
        log.info("Reservation details set payment done: {}", reservationDetail);
    }

    /**
     * Marks a single ticket as paid / unpaid. The reservation becomes PAID only once every
     * ticket in it is paid (or checked in); otherwise it stays PAYMENT_PENDING.
     * Returns the reservation, reloaded with details, for the UI to refresh.
     */
    @Transactional
    public ReservationDetail setTicketPaid(Long seatReservationId, boolean paid) {
        Long eventId = eventContext.requireEditable().getId();
        SeatReservation sr = seatReservationRepository.findByIdAndEventId(seatReservationId, eventId)
                .orElseThrow(() -> new IllegalStateException("Ticket no longer exists"));
        if (sr.getReservationStatus() == ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Ticket " + sr.getTicketNo() + " is already checked in");
        }
        sr.setReservationStatus(paid ? ReservationStatus.PAID : ReservationStatus.PAYMENT_PENDING);
        seatReservationRepository.save(sr);
        log.info("Ticket {} marked {}", sr.getTicketNo(), sr.getReservationStatus());

        ReservationDetail rd = reservationDetailRepository.findBySeatReservationId(eventId, seatReservationId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found for ticket " + sr.getTicketNo()));
        boolean allPaid = rd.getSeatReservations().stream()
                .allMatch(s -> s.getReservationStatus() == ReservationStatus.PAID
                        || s.getReservationStatus() == ReservationStatus.CHECKED_IN);
        rd.setReservationStatus(allPaid ? ReservationStatus.PAID : ReservationStatus.PAYMENT_PENDING);
        reservationDetailRepository.save(rd);
        return reservationDetailRepository.findByIdWithDetails(eventId, rd.getId()).orElse(rd);
    }

    @Transactional
    public ReservationDetail findByIdWithDetails(Long id) {
        return id == null ? null : reservationDetailRepository.findByIdWithDetails(eventContext.currentId(), id).orElse(null);
    }

    /** Single ticket rendered with the current layout, for in-app preview. */
    public byte[] renderTicketImage(SeatReservation sr) throws IOException {
        return ticketService.generateOnTheFly(sr.getTicketNo(), buildTableSeatLabel(sr));
    }

    // Find the parent reservation by a seat ticket number
    @Transactional
    public ReservationDetail findReservationByTicketNo(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) return null;
        return reservationDetailRepository.findByTicketNoWithDetails(eventContext.currentId(), ticketNo).stream().findFirst().orElse(null);
    }

    /**
     * Lets a seller keep a free-text buyer name against one of their own tickets, for tracking.
     * Ownership is re-verified server-side against sellerId — never trust the caller's own copy.
     */
    @Transactional
    public boolean updateBuyerName(SellerDetail seller, Long seatReservationId, String buyerName) {
        if (seller == null || seller.getId() == null || seller.getEvent() == null || seatReservationId == null) return false;
        Long sellerId = seller.getId();
        for (ReservationDetail rd : reservationDetailRepository.findAllWithDetailsBySeller(seller.getEvent().getId(), sellerId)) {
            if (rd.getSellerDetail() == null || !sellerId.equals(rd.getSellerDetail().getId())) continue;
            if (rd.getSeatReservations() == null) continue;
            EventContext.requireEditable(rd.getEvent());
            for (SeatReservation sr : rd.getSeatReservations()) {
                if (sr != null && seatReservationId.equals(sr.getId())) {
                    String trimmed = buyerName != null ? buyerName.trim() : null;
                    sr.setBuyerName(trimmed != null && !trimmed.isEmpty() ? trimmed : null);
                    seatReservationRepository.save(sr);
                    return true;
                }
            }
        }
        return false;
    }

    // Mark a seat reservation as CHECKED_IN using the scanned ticket number
    @Transactional
    public boolean checkInByTicketNo(String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) return false;
        Long eventId = eventContext.requireActive().getId();
        SeatReservation target = findSeatReservationByTicketNo(eventId, ticketNo);
        if (target == null) {
            log.warn("No seat reservation found for ticket {}", ticketNo);
            return false;
        }
        if (target.getReservationStatus() == ReservationStatus.CHECKED_IN) {
            log.info("Ticket {} already checked-in", ticketNo);
            return false;
        }
        if (target.getReservationStatus() != ReservationStatus.PAID) {
            log.info("Ticket {} not paid, check-in refused", ticketNo);
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

    /** Reverts a checked-in ticket to PAID and its seat to RESERVED. */
    @Transactional
    public boolean undoCheckIn(String ticketNo) {
        Long eventId = eventContext.requireActive().getId();
        SeatReservation target = findSeatReservationByTicketNo(eventId, ticketNo);
        if (target == null || target.getReservationStatus() != ReservationStatus.CHECKED_IN) return false;
        target.setReservationStatus(ReservationStatus.PAID);
        seatReservationRepository.save(target);
        if (target.getSeatDetail() != null) {
            target.getSeatDetail().setSeatStatus(SeatStatus.RESERVED);
            seatDetailRepository.save(target.getSeatDetail());
        }
        log.info("Undid check-in for ticket {}", ticketNo);
        return true;
    }

    /** Checks in every PAID (not yet checked-in) ticket of the given reservation. Returns how many were checked in. */
    @Transactional
    public int checkInAllPaid(Long reservationId) {
        Long eventId = eventContext.requireActive().getId();
        ReservationDetail rd = reservationDetailRepository.findByIdWithDetails(eventId, reservationId).orElse(null);
        if (rd == null || rd.getSeatReservations() == null) return 0;
        int n = 0;
        for (SeatReservation sr : rd.getSeatReservations()) {
            if (sr.getReservationStatus() != ReservationStatus.PAID) continue;
            sr.setReservationStatus(ReservationStatus.CHECKED_IN);
            seatReservationRepository.save(sr);
            if (sr.getSeatDetail() != null) {
                sr.getSeatDetail().setSeatStatus(SeatStatus.CHECKED_IN);
                seatDetailRepository.save(sr.getSeatDetail());
            }
            n++;
        }
        log.info("Group check-in for reservation {}: {} ticket(s)", rd.getReferenceNo(), n);
        return n;
    }

    private SeatReservation findSeatReservationByTicketNo(Long eventId, String ticketNo) {
        if (ticketNo == null || ticketNo.isBlank()) return null;
        ReservationDetail rd = reservationDetailRepository.findByTicketNoWithDetails(eventId, ticketNo).stream().findFirst().orElse(null);
        if (rd == null || rd.getSeatReservations() == null) return null;
        return rd.getSeatReservations().stream()
                .filter(sr -> sr != null && ticketNo.equals(sr.getTicketNo()))
                .findFirst().orElse(null);
    }
}
