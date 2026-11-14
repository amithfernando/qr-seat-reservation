package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.model.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Seat reservations are scoped through their seat's table (seatDetail.tableDetail.event). */
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Query("SELECT COUNT(sr) FROM SeatReservation sr WHERE sr.seatDetail.tableDetail.event.id = :eventId AND sr.reservationStatus = :status")
    long countByReservationStatus(@Param("eventId") Long eventId, @Param("status") ReservationStatus status);

    @Query("SELECT COUNT(sr) FROM SeatReservation sr WHERE sr.seatDetail.tableDetail.event.id = :eventId " +
           "AND sr.reservationStatus = :status AND sr.ticketType = :ticketType")
    long countByReservationStatusAndTicketType(
            @Param("eventId") Long eventId,
            @Param("status") ReservationStatus status,
            @Param("ticketType") TicketType ticketType);

    @Query("""
            SELECT sr.ticketType AS ticketType,
               sr.ticketPrice AS ticketPrice,
               sr.ticketCategory.price AS categoryPrice,
               COUNT(sr) AS ticketCount
            FROM SeatReservation sr
            WHERE sr.seatDetail.tableDetail.event.id = :eventId
              AND sr.reservationStatus = :status
              AND (sr.ticketType IS NULL OR sr.ticketType <> :excludedTicketType)
            GROUP BY sr.ticketType, sr.ticketPrice, sr.ticketCategory.price
            """)
    List<RevenueBreakdown> summarizeRevenueByStatusExcludingTicketType(
            @Param("eventId") Long eventId,
            @Param("status") ReservationStatus status,
            @Param("excludedTicketType") TicketType excludedTicketType);

    @Query("SELECT sr FROM SeatReservation sr LEFT JOIN FETCH sr.seatDetail sd LEFT JOIN FETCH sd.tableDetail td " +
           "WHERE td.event.id = :eventId ORDER BY td.tableName, sd.seatNo")
    List<SeatReservation> findAllWithSeatDetails(@Param("eventId") Long eventId);

    @Query("SELECT sr FROM SeatReservation sr WHERE sr.id = :id AND sr.seatDetail.tableDetail.event.id = :eventId")
    Optional<SeatReservation> findByIdAndEventId(@Param("id") Long id, @Param("eventId") Long eventId);

    interface RevenueBreakdown {
        TicketType getTicketType();
        Double getTicketPrice();
        Double getCategoryPrice();
        Long getTicketCount();
    }
}
