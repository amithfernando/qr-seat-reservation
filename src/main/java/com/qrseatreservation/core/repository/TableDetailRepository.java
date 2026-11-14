package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableDetailRepository extends JpaRepository<TableDetail, Long> {
    List<TableDetail> findByEventIdOrderByIdAsc(Long eventId);

    Optional<TableDetail> findByIdAndEventId(Long id, Long eventId);

    boolean existsByEventIdAndTableName(Long eventId, String tableName);
}
