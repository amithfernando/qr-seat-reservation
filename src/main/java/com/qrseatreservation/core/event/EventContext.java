package com.qrseatreservation.core.event;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.EventRepository;
import com.qrseatreservation.core.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Resolves the event that the current UI session works on.
 * <p>
 * After login every user must explicitly select an event (see SelectEventView). The selection is stored in the
 * user's own session and re-validated on every call, so revoking an assignment or archiving an event takes effect
 * immediately:
 * <ul>
 *   <li>ADMIN users may select any event (archived ones only from the Events page, read-only).</li>
 *   <li>All other users may only select non-archived events assigned to them in User management.</li>
 * </ul>
 * There is deliberately no fallback to "the active event": without a valid selection, access is denied.
 */
@Component
@Slf4j
public class EventContext {

    static final String SESSION_KEY = EventContext.class.getName() + ".selectedEventId";
    public static final String NO_SELECTION = "No event selected. Please select an event.";

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventContext(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /** The event the caller is currently working on. */
    public Event current() {
        Long selected = selectedEventId();
        if (selected == null) {
            throw new EventAccessException(NO_SELECTION);
        }
        Event event = eventRepository.findById(selected).orElse(null);
        if (event == null || !canAccess(event)) {
            clearSelection();
            throw new EventAccessException("You no longer have access to the selected event. Please select an event.");
        }
        return event;
    }

    public Long currentId() {
        return current().getId();
    }

    /** True if the session has a selected event that the user may still access. */
    public boolean hasValidSelection() {
        try {
            current();
            return true;
        } catch (EventAccessException e) {
            return false;
        }
    }

    /** Events offered in the event picker after login: never archived; non-admins only see their assigned events. */
    public List<Event> selectableEvents() {
        if (!isAuthenticated()) return List.of();
        return isAdmin() ? eventRepository.findAllNotArchived() : eventRepository.findAssignedNotArchived(currentUser());
    }

    /** Whether the current user may work on the given event. */
    public boolean canAccess(Event event) {
        if (event == null || !isAuthenticated()) return false;
        if (isAdmin()) return true;
        return !event.isArchived() && userRepository.isAssignedToEvent(currentUser(), event.getId());
    }

    /** Current event, which must not be archived (archived events are read-only). */
    public Event requireEditable() {
        return requireEditable(current());
    }

    public static Event requireEditable(Event event) {
        if (event.isArchived()) {
            throw new EventAccessException("Event '" + event.getName() + "' is archived and read-only.");
        }
        return event;
    }

    /** Current event, which must be the ACTIVE event (used for check-in at the door). */
    public Event requireActive() {
        Event event = current();
        if (!event.isActive()) {
            throw new EventAccessException("Check-in is only possible for the active event. '"
                    + event.getName() + "' is " + event.getStatus() + ".");
        }
        return event;
    }

    /** Guards against acting on an entity that belongs to another event than the current one. */
    public void assertBelongsToCurrent(Event owner) {
        assertBelongsTo(owner, currentId());
    }

    public static void assertBelongsTo(Event owner, Long eventId) {
        if (owner == null || eventId == null || !Objects.equals(owner.getId(), eventId)) {
            throw new EventAccessException("The requested record does not belong to the current event.");
        }
    }

    /** Selects the event for the user's own UI session, after checking that the user may access it. */
    public void select(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventAccessException("Event not found."));
        if (!canAccess(event)) {
            log.warn("User '{}' was denied access to event id={}", currentUser(), eventId);
            throw new EventAccessException("You do not have access to this event.");
        }
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException("No UI session to store the selected event in.");
        }
        session.setAttribute(SESSION_KEY, event.getId());
        log.info("User '{}' selected event id={} '{}'", currentUser(), event.getId(), event.getName());
    }

    public void clearSelection() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) session.setAttribute(SESSION_KEY, null);
    }

    private Long selectedEventId() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) return null;
        Object value = session.getAttribute(SESSION_KEY);
        return value instanceof Long id ? id : null;
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticated() && auth.getAuthorities() != null
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
