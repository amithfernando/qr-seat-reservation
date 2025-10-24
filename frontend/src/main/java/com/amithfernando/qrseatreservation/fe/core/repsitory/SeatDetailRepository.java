package com.amithfernando.qrseatreservation.fe.core.repsitory;

import com.amithfernando.qrseatreservation.fe.core.model.SeatDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatDetailRepository extends JpaRepository<SeatDetail, Long> {
}
