package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    @Query("SELECT tc FROM TicketCategory tc WHERE tc.event.id = :eventId AND tc.active = true ORDER BY tc.name")
    List<TicketCategory> findActiveByEvent(@Param("eventId") Long eventId);

    @Query("SELECT tc FROM TicketCategory tc WHERE tc.event.id = :eventId AND tc.name = :name")
    Optional<TicketCategory> findByEventAndName(@Param("eventId") Long eventId, @Param("name") String name);

    @Query("SELECT tc FROM TicketCategory tc WHERE tc.event.id = :eventId ORDER BY tc.name")
    List<TicketCategory> findAllByEventOrdered(@Param("eventId") Long eventId);

    Optional<TicketCategory> findByIdAndEventId(Long id, Long eventId);

    long countByEventId(Long eventId);
}
