package com.amithfernando.qrseatreservation.api.repsitory;

import com.amithfernando.qrseatreservation.api.model.SeatDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatDetailRepository extends JpaRepository<SeatDetail, Long> {
}
