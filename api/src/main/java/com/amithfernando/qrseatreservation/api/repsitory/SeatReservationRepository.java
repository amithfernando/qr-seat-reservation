package com.amithfernando.qrseatreservation.api.repsitory;

import com.amithfernando.qrseatreservation.api.model.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
}
