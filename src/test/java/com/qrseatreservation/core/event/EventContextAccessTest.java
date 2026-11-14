package com.qrseatreservation.core.event;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.EventRepository;
import com.qrseatreservation.core.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Access rules for event selection: explicit selection required, assignments enforced for non-admins. */
class EventContextAccessTest {

    private final EventRepository eventRepository = mock(EventRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final EventContext context = new EventContext(eventRepository, userRepository);
    private final Map<String, Object> attributes = new HashMap<>();

    private final Event active = Event.builder().id(1L).name("Active").status(EventStatus.ACTIVE).build();
    private final Event draft = Event.builder().id(2L).name("Draft").status(EventStatus.DRAFT).build();
    private final Event archived = Event.builder().id(3L).name("Old").status(EventStatus.ARCHIVED).build();

    @BeforeEach
    void setUp() {
        VaadinSession session = mock(VaadinSession.class);
        doAnswer(inv -> attributes.put(inv.getArgument(0), inv.getArgument(1)))
                .when(session).setAttribute(anyString(), any());
        when(session.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.<String>getArgument(0)));
        VaadinSession.setCurrent(session);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(active));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(draft));
        when(eventRepository.findById(3L)).thenReturn(Optional.of(archived));
    }

    @AfterEach
    void tearDown() {
        VaadinSession.setCurrent(null);
        SecurityContextHolder.clearContext();
    }

    private static void login(String user, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void current_withoutSelection_isDenied_evenWhenAnActiveEventExists() {
        login("door", "ENTRANCE");
        assertThatThrownBy(context::current).isInstanceOf(EventAccessException.class);
        assertThat(context.hasValidSelection()).isFalse();
    }

    @Test
    void entrance_canSelectAssignedEvent() {
        login("door", "ENTRANCE");
        when(userRepository.isAssignedToEvent("door", 1L)).thenReturn(true);
        context.select(1L);
        assertThat(context.current()).isEqualTo(active);
    }

    @Test
    void entrance_cannotSelectUnassignedEvent() {
        login("door", "ENTRANCE");
        when(userRepository.isAssignedToEvent("door", 2L)).thenReturn(false);
        assertThatThrownBy(() -> context.select(2L)).isInstanceOf(EventAccessException.class);
        assertThat(context.hasValidSelection()).isFalse();
    }

    @Test
    void entrance_cannotSelectArchivedEvent_evenIfAssigned() {
        login("door", "ENTRANCE");
        when(userRepository.isAssignedToEvent("door", 3L)).thenReturn(true);
        assertThatThrownBy(() -> context.select(3L)).isInstanceOf(EventAccessException.class);
    }

    @Test
    void revokedAssignment_takesEffectImmediately() {
        login("door", "ENTRANCE");
        when(userRepository.isAssignedToEvent("door", 1L)).thenReturn(true);
        context.select(1L);
        when(userRepository.isAssignedToEvent("door", 1L)).thenReturn(false);
        assertThatThrownBy(context::current).isInstanceOf(EventAccessException.class);
        assertThat(attributes.get(EventContext.SESSION_KEY)).isNull();
    }

    @Test
    void admin_canSelectAnyEvent_includingArchivedReadOnly() {
        login("boss", "ADMIN");
        context.select(2L);
        assertThat(context.current()).isEqualTo(draft);
        context.select(3L);
        assertThat(context.current()).isEqualTo(archived);
        assertThatThrownBy(context::requireEditable).isInstanceOf(EventAccessException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void selectableEvents_adminGetsAllNonArchived_userGetsOnlyAssignedNonArchived() {
        when(eventRepository.findAllNotArchived()).thenReturn(List.of(draft, active));
        when(eventRepository.findAssignedNotArchived("door")).thenReturn(List.of(active));

        login("boss", "ADMIN");
        assertThat(context.selectableEvents()).containsExactly(draft, active);

        login("door", "ENTRANCE");
        assertThat(context.selectableEvents()).containsExactly(active);
    }

    @Test
    void anonymous_getsNothing() {
        SecurityContextHolder.clearContext();
        assertThat(context.selectableEvents()).isEmpty();
        assertThatThrownBy(() -> context.select(1L)).isInstanceOf(EventAccessException.class);
    }
}
