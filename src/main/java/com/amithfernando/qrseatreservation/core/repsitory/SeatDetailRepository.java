package com.amithfernando.qrseatreservation.core.repsitory;

import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatDetailRepository extends JpaRepository<SeatDetail, Long> {
}
