package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;

import com.qrseatreservation.core.model.VenueZone;
import com.qrseatreservation.core.repository.VenueZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueZoneService {

    private final VenueZoneRepository repository;
    private final EventContext eventContext;

    public List<VenueZone> getAll() {
        return repository.findByEventIdOrderByIdAsc(eventContext.currentId());
    }

    public VenueZone save(VenueZone zone) {
        Event event = eventContext.requireEditable();
        if (zone.getId() == null) {
            zone.setEvent(event);
        } else {
            VenueZone existing = repository.findByIdAndEventId(zone.getId(), event.getId())
                    .orElseThrow(() -> new EventAccessException("Zone does not belong to the current event"));
            zone.setEvent(existing.getEvent());
        }
        return repository.save(zone);
    }

    public void delete(Long id) {
        VenueZone zone = repository.findByIdAndEventId(id, eventContext.requireEditable().getId())
                .orElseThrow(() -> new EventAccessException("Zone does not belong to the current event"));
        repository.delete(zone);
    }
}
