package com.amithfernando.qrseatreservation.api.repsitory;

import com.amithfernando.qrseatreservation.api.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableDetailRepository extends JpaRepository<TableDetail, Long> {
    Optional<TableDetail> findByTableName(String tableName);
}
