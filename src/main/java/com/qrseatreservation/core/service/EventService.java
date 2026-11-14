package com.qrseatreservation.core.service;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.*;
import com.qrseatreservation.core.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lifecycle of ticket campaigns: create (optionally copying setup from another event), activate, archive.
 * Several events can be ACTIVE at once; each user session works on one selected event. Archiving is explicit
 * (optionally done while activating another event) and makes an event read-only.
 */
@Service
@Slf4j
public class EventService {

    /** What to copy from a source event when creating a new one. Tickets and reservations are never copied. */
    public enum CopyOption { TEMPLATE, CATEGORIES, TABLES, SELLERS }

    private final EventRepository eventRepository;
    private final SettingRepository settingRepository;
    private final SettingService settingService;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoryService ticketCategoryService;
    private final TableDetailRepository tableDetailRepository;
    private final VenueZoneRepository venueZoneRepository;
    private final SellerDetailRepository sellerDetailRepository;
    private final TicketRepository ticketRepository;

    public EventService(EventRepository eventRepository, SettingRepository settingRepository, SettingService settingService,
                        TicketCategoryRepository ticketCategoryRepository, TicketCategoryService ticketCategoryService,
                        TableDetailRepository tableDetailRepository,
                        VenueZoneRepository venueZoneRepository, SellerDetailRepository sellerDetailRepository,
                        TicketRepository ticketRepository) {
        this.eventRepository = eventRepository;
        this.settingRepository = settingRepository;
        this.settingService = settingService;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketCategoryService = ticketCategoryService;
        this.tableDetailRepository = tableDetailRepository;
        this.venueZoneRepository = venueZoneRepository;
        this.sellerDetailRepository = sellerDetailRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<Event> listEvents() {
        return eventRepository.findAllNewestFirst();
    }

    public long countTickets(Long eventId) {
        return ticketRepository.countByEventId(eventId);
    }

    /** Creates a DRAFT event. When {@code sourceEventId} is given, the selected parts of its setup are copied. */
    @Transactional
    public Event createEvent(String name, String venue, LocalDate eventDate, Long sourceEventId, Set<CopyOption> options) {
        requireAdmin();
        String cleanName = requireName(name);
        Set<CopyOption> copy = options == null || options.isEmpty() ? EnumSet.noneOf(CopyOption.class) : EnumSet.copyOf(options);
        Event source = null;
        if (sourceEventId != null && !copy.isEmpty()) {
            source = eventRepository.findById(sourceEventId)
                    .orElseThrow(() -> new IllegalArgumentException("Source event not found"));
        }

        String user = EventContext.currentUser();
        Event event = eventRepository.save(Event.builder()
                .name(cleanName)
                .venue(trimToNull(venue))
                .eventDate(eventDate)
                .status(EventStatus.DRAFT)
                .createdBy(user)
                .updatedBy(user)
                .build());

        Setting sourceSetting = source != null ? settingRepository.findByEventId(source.getId()).orElse(null) : null;
        if (sourceSetting != null && copy.contains(CopyOption.TEMPLATE)) {
            settingService.copySetting(sourceSetting, event);
        } else {
            settingService.createDefaultSetting(event);
        }

        if (source != null && copy.contains(CopyOption.CATEGORIES)) {
            copyCategories(source, event);
        } else {
            ticketCategoryService.initializeDefaultCategories(event);
        }
        if (source != null && copy.contains(CopyOption.TABLES)) {
            copyTablesAndZones(source, event);
        }
        if (source != null && copy.contains(CopyOption.SELLERS)) {
            copySellers(source, event);
        }

        log.info("User '{}' created event id={} '{}' (copied {} from event {})", user, event.getId(), event.getName(),
                copy, source != null ? source.getId() : null);
        return event;
    }

    /** Makes a DRAFT event ACTIVE; other active events stay active. */
    @Transactional
    public Event activate(Long eventId) {
        return activate(eventId, false);
    }

    /**
     * Makes a DRAFT event ACTIVE. With {@code archiveOtherActive} every other currently ACTIVE event is archived
     * (read-only, check-in and seller links stop) in the same transaction.
     */
    @Transactional
    public Event activate(Long eventId, boolean archiveOtherActive) {
        requireAdmin();
        Event target = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (target.isActive()) return target;
        if (target.getStatus() != EventStatus.DRAFT) {
            throw new EventAccessException("Only draft events can be activated. Archived events stay read-only.");
        }
        String user = EventContext.currentUser();
        LocalDateTime now = LocalDateTime.now();
        // Target (and, when archiving, the other ACTIVE rows) are locked so concurrent activate/archive calls serialise
        List<Event> others = archiveOtherActive ? eventRepository.findByStatusForUpdate(EventStatus.ACTIVE) : List.of();
        for (Event current : others) {
            if (current.getId().equals(target.getId())) continue;
            current.setStatus(EventStatus.ARCHIVED);
            current.setArchivedAt(now);
            current.setUpdatedBy(user);
            eventRepository.saveAndFlush(current);
            log.info("User '{}' archived event id={} '{}'", user, current.getId(), current.getName());
        }
        target.setStatus(EventStatus.ACTIVE);
        target.setActivatedAt(now);
        target.setUpdatedBy(user);
        eventRepository.saveAndFlush(target);
        log.info("User '{}' activated event id={} '{}'", user, target.getId(), target.getName());
        return target;
    }

    /** Archives an ACTIVE or DRAFT event. Archived events are read-only and their seller portal links stop working. */
    @Transactional
    public Event archive(Long eventId) {
        requireAdmin();
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (event.isArchived()) return event;
        String user = EventContext.currentUser();
        event.setStatus(EventStatus.ARCHIVED);
        event.setArchivedAt(LocalDateTime.now());
        event.setUpdatedBy(user);
        eventRepository.saveAndFlush(event);
        log.info("User '{}' archived event id={} '{}'", user, event.getId(), event.getName());
        return event;
    }

    /** Updates name / venue / date of a non-archived event (also updates the display name used in Settings). */
    @Transactional
    public Event updateDetails(Long eventId, String name, String venue, LocalDate eventDate) {
        requireAdmin();
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        EventContext.requireEditable(event);
        event.setName(requireName(name));
        event.setVenue(trimToNull(venue));
        event.setEventDate(eventDate);
        event.setUpdatedBy(EventContext.currentUser());
        eventRepository.save(event);
        settingRepository.findByEventId(eventId).ifPresent(s -> {
            s.setEventName(event.getName());
            s.setVenue(event.getVenue());
            settingRepository.save(s);
        });
        return event;
    }

    private void copyCategories(Event source, Event target) {
        List<TicketCategory> categories = ticketCategoryRepository.findAllByEventOrdered(source.getId());
        if (categories.isEmpty()) {
            ticketCategoryService.initializeDefaultCategories(target);
            return;
        }
        for (TicketCategory c : categories) {
            ticketCategoryRepository.save(TicketCategory.builder()
                    .event(target)
                    .name(c.getName())
                    .price(c.getPrice())
                    .description(c.getDescription())
                    .active(c.getActive())
                    .build());
        }
    }

    private void copyTablesAndZones(Event source, Event target) {
        for (TableDetail t : tableDetailRepository.findByEventIdOrderByIdAsc(source.getId())) {
            TableDetail copy = TableDetail.builder()
                    .event(target)
                    .tableName(t.getTableName())
                    .description(t.getDescription())
                    .noOfSeats(t.getNoOfSeats())
                    .posX(t.getPosX())
                    .posY(t.getPosY())
                    .build();
            List<SeatDetail> seats = new ArrayList<>();
            if (t.getSeatDetails() != null) {
                for (SeatDetail s : t.getSeatDetails()) {
                    // Blocked seats stay blocked; reserved / checked-in seats start fresh as available
                    SeatStatus status = s.getSeatStatus() == SeatStatus.UNAVAILABLE ? SeatStatus.UNAVAILABLE : SeatStatus.AVAILABLE;
                    seats.add(SeatDetail.builder().seatNo(s.getSeatNo()).seatStatus(status).tableDetail(copy).build());
                }
            }
            copy.setSeatDetails(seats);
            tableDetailRepository.save(copy); // seats are persisted via cascade
        }
        for (VenueZone z : venueZoneRepository.findByEventIdOrderByIdAsc(source.getId())) {
            venueZoneRepository.save(VenueZone.builder()
                    .event(target)
                    .label(z.getLabel())
                    .zoneType(z.getZoneType())
                    .posx(z.getPosx())
                    .posy(z.getPosy())
                    .width(z.getWidth())
                    .height(z.getHeight())
                    .rotation(z.getRotation())
                    .color(z.getColor())
                    .build());
        }
    }

    private void copySellers(Event source, Event target) {
        for (SellerDetail s : sellerDetailRepository.findByEventIdOrderByIdAsc(source.getId())) {
            boolean hadPortal = s.getAccessToken() != null && s.getPasswordHash() != null;
            sellerDetailRepository.save(SellerDetail.builder()
                    .event(target)
                    .name(s.getName())
                    .address(s.getAddress())
                    .email(s.getEmail())
                    .phone(s.getPhone())
                    .description(s.getDescription())
                    // New random portal link per event; the seller keeps the same portal password
                    .accessToken(hadPortal ? UUID.randomUUID().toString() : null)
                    .passwordHash(hadPortal ? s.getPasswordHash() : null)
                    .build());
        }
    }

    private static void requireAdmin() {
        if (!EventContext.isAdmin()) {
            throw new EventAccessException("Only administrators can manage events.");
        }
    }

    private static String requireName(String name) {
        String n = trimToNull(name);
        if (n == null) throw new IllegalArgumentException("Event name is required");
        if (n.length() > 255) throw new IllegalArgumentException("Event name is too long (max 255 characters)");
        return n;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
