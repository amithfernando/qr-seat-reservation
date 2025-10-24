package com.amithfernando.qrseatreservation.fe.core.repsitory;

import com.amithfernando.qrseatreservation.fe.core.model.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
}
