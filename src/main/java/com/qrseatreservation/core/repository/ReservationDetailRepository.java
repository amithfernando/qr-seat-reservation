package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.ReservationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "LEFT JOIN FETCH rd.sellerDetail " +
           "LEFT JOIN FETCH rd.seatReservations sr " +
           "LEFT JOIN FETCH sr.seatDetail sd " +
           "LEFT JOIN FETCH sd.tableDetail " +
           "WHERE rd.event.id = :eventId")
    List<ReservationDetail> findAllWithDetails(@Param("eventId") Long eventId);

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "LEFT JOIN FETCH rd.sellerDetail " +
           "LEFT JOIN FETCH rd.seatReservations sr " +
           "LEFT JOIN FETCH sr.seatDetail sd " +
           "LEFT JOIN FETCH sd.tableDetail " +
           "WHERE rd.event.id = :eventId AND rd.sellerDetail.id = :sellerId")
    List<ReservationDetail> findAllWithDetailsBySeller(@Param("eventId") Long eventId, @Param("sellerId") Long sellerId);

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "LEFT JOIN FETCH rd.sellerDetail " +
           "LEFT JOIN FETCH rd.seatReservations sr " +
           "LEFT JOIN FETCH sr.seatDetail sd " +
           "LEFT JOIN FETCH sd.tableDetail " +
           "WHERE rd.event.id = :eventId AND rd.id = :id")
    Optional<ReservationDetail> findByIdWithDetails(@Param("eventId") Long eventId, @Param("id") Long id);

    @Query("SELECT rd FROM ReservationDetail rd JOIN rd.seatReservations sr WHERE rd.event.id = :eventId AND sr.id = :seatReservationId")
    Optional<ReservationDetail> findBySeatReservationId(@Param("eventId") Long eventId, @Param("seatReservationId") Long seatReservationId);

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "LEFT JOIN FETCH rd.sellerDetail " +
           "LEFT JOIN FETCH rd.seatReservations sr " +
           "LEFT JOIN FETCH sr.seatDetail sd " +
           "LEFT JOIN FETCH sd.tableDetail " +
           "WHERE rd.event.id = :eventId AND rd.id IN " +
           "(SELECT rd2.id FROM ReservationDetail rd2 JOIN rd2.seatReservations sr2 WHERE rd2.event.id = :eventId AND sr2.ticketNo = :ticketNo)")
    List<ReservationDetail> findByTicketNoWithDetails(@Param("eventId") Long eventId, @Param("ticketNo") String ticketNo);

    Optional<ReservationDetail> findByIdAndEventId(Long id, Long eventId);
}
