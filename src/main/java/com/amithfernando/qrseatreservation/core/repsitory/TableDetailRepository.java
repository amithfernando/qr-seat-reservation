package com.amithfernando.qrseatreservation.core.repsitory;

import com.amithfernando.qrseatreservation.core.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableDetailRepository extends JpaRepository<TableDetail, Long> {
}
