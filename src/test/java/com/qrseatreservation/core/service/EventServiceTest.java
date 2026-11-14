package com.qrseatreservation.core.service;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private SettingRepository settingRepository;
    @Mock private SettingService settingService;
    @Mock private TicketCategoryRepository ticketCategoryRepository;
    @Mock private TicketCategoryService ticketCategoryService;
    @Mock private TableDetailRepository tableDetailRepository;
    @Mock private VenueZoneRepository venueZoneRepository;
    @Mock private SellerDetailRepository sellerDetailRepository;
    @Mock private TicketRepository ticketRepository;

    private EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(eventRepository, settingRepository, settingService, ticketCategoryRepository,
                ticketCategoryService, tableDetailRepository, venueZoneRepository, sellerDetailRepository, ticketRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void loginAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void activate_keepsOtherActiveEventsActive_byDefault() {
        loginAsAdmin();
        Event current = Event.builder().id(1L).name("Old").status(EventStatus.ACTIVE).build();
        Event target = Event.builder().id(2L).name("New").status(EventStatus.DRAFT).build();
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));

        Event result = service.activate(2L);

        assertThat(result).isSameAs(target);
        assertThat(target.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(current.getStatus()).isEqualTo(EventStatus.ACTIVE);
        verify(eventRepository, never()).findByStatusForUpdate(any());
        verify(eventRepository, never()).saveAndFlush(current);
    }

    @Test
    void activate_withArchiveOthers_archivesCurrentActive_andActivatesDraftTarget() {
        loginAsAdmin();
        Event current = Event.builder().id(1L).name("Old").status(EventStatus.ACTIVE).build();
        Event target = Event.builder().id(2L).name("New").status(EventStatus.DRAFT).build();
        when(eventRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(eventRepository.findByStatusForUpdate(EventStatus.ACTIVE)).thenReturn(List.of(current));

        Event result = service.activate(2L, true);

        assertThat(result).isSameAs(target);
        assertThat(current.getStatus()).isEqualTo(EventStatus.ARCHIVED);
        assertThat(current.getArchivedAt()).isNotNull();
        assertThat(target.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(target.getActivatedAt()).isNotNull();
        var order = inOrder(eventRepository);
        order.verify(eventRepository).saveAndFlush(current);
        order.verify(eventRepository).saveAndFlush(target);
    }

    @Test
    void activate_rejectsArchivedEvent() {
        loginAsAdmin();
        Event archived = Event.builder().id(3L).name("Past").status(EventStatus.ARCHIVED).build();
        when(eventRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> service.activate(3L)).isInstanceOf(EventAccessException.class);
        assertThat(archived.getStatus()).isEqualTo(EventStatus.ARCHIVED);
        verify(eventRepository, never()).saveAndFlush(any());
    }

    @Test
    void activate_withoutAdmin_throws() {
        assertThatThrownBy(() -> service.activate(2L)).isInstanceOf(EventAccessException.class);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void createEvent_withoutAdmin_throws() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "door", null, List.of(new SimpleGrantedAuthority("ROLE_ENTRANCE"))));

        assertThatThrownBy(() -> service.createEvent("Gala", null, null, null, Set.of()))
                .isInstanceOf(EventAccessException.class);
        verifyNoInteractions(eventRepository, settingService, ticketCategoryService);
    }

    @Test
    void createEvent_asAdmin_createsDraftWithDefaults() {
        loginAsAdmin();
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });

        Event created = service.createEvent("  Gala  ", "Hall", null, null, null);

        assertThat(created.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(created.getName()).isEqualTo("Gala");
        assertThat(created.getCreatedBy()).isEqualTo("admin");
        verify(settingService).createDefaultSetting(created);
        verify(ticketCategoryService).initializeDefaultCategories(created);
    }
}
