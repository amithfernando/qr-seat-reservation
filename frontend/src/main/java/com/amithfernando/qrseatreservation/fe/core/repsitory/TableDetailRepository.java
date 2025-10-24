package com.amithfernando.qrseatreservation.fe.core.repsitory;

import com.amithfernando.qrseatreservation.fe.core.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableDetailRepository extends JpaRepository<TableDetail, Long> {
}
