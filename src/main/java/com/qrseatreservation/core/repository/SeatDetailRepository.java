package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.model.SeatDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeatDetailRepository extends JpaRepository<SeatDetail, Long> {

    @Query("SELECT COUNT(sd) FROM SeatDetail sd WHERE sd.tableDetail.event.id = :eventId AND sd.seatStatus = :status")
    long countByStatus(@Param("eventId") Long eventId, @Param("status") SeatStatus status);

    @Query("SELECT COUNT(sd) FROM SeatDetail sd WHERE sd.tableDetail.event.id = :eventId")
    long countByEvent(@Param("eventId") Long eventId);

    @Query("SELECT sd FROM SeatDetail sd WHERE sd.id = :id AND sd.tableDetail.event.id = :eventId")
    Optional<SeatDetail> findByIdAndEventId(@Param("id") Long id, @Param("eventId") Long eventId);
}
