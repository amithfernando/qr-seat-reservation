package com.amithfernando.qrseatreservation.api.repsitory;

import com.amithfernando.qrseatreservation.api.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableDetailRepository extends JpaRepository<TableDetail, Long> {
}
