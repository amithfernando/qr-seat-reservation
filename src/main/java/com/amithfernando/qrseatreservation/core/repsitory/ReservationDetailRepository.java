package com.amithfernando.qrseatreservation.core.repsitory;

import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "LEFT JOIN FETCH rd.sellerDetail " +
           "LEFT JOIN FETCH rd.seatReservations sr " +
           "LEFT JOIN FETCH sr.seatDetail sd " +
           "LEFT JOIN FETCH sd.tableDetail")
    List<ReservationDetail> findAllWithDetails();

    @Query("SELECT DISTINCT rd FROM ReservationDetail rd " +
           "JOIN rd.seatReservations sr " +
           "JOIN sr.seatDetail sd " +
           "JOIN sd.tableDetail td " +
           "WHERE td.id = :tableId")
    List<ReservationDetail> findByTableDetailId(Long tableId);
}
