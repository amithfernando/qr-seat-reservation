package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.VenueZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueZoneRepository extends JpaRepository<VenueZone, Long> {
    List<VenueZone> findByEventIdOrderByIdAsc(Long eventId);

    Optional<VenueZone> findByIdAndEventId(Long id, Long eventId);
}
