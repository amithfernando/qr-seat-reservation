package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.model.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findFirstByStatus(EventStatus status);

    @Query("SELECT e FROM Event e ORDER BY e.id DESC")
    List<Event> findAllNewestFirst();

    @Query("SELECT e FROM Event e WHERE e.status <> com.qrseatreservation.core.enums.EventStatus.ARCHIVED ORDER BY e.id DESC")
    List<Event> findAllNotArchived();

    @Query("SELECT e FROM User u JOIN u.events e WHERE u.username = :username " +
           "AND e.status <> com.qrseatreservation.core.enums.EventStatus.ARCHIVED ORDER BY e.id DESC")
    List<Event> findAssignedNotArchived(@org.springframework.data.repository.query.Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.status = :status")
    List<Event> findByStatusForUpdate(EventStatus status);
}
