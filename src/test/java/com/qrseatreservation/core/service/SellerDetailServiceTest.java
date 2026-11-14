package com.qrseatreservation.core.service;


import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.repository.SellerDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerDetailServiceTest {

    @Mock
    private SellerDetailRepository repository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private EventContext eventContext;

    private SellerDetailService service;

    private final Event event = Event.builder().id(1L).name("E").status(EventStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        service = new SellerDetailService(repository, passwordEncoder, eventContext);
    }

    @Test
    void saveSeller_delegatesToRepository() {
        SellerDetail seller = SellerDetail.builder()
                .name("Alice")
                .address("123 Street")
                .email("alice@example.com")
                .phone("+100000000")
                .description("VIP")
                .build();
        when(eventContext.requireEditable()).thenReturn(event);

        service.saveSeller(seller);

        verify(repository, times(1)).save(seller);
        assertThat(seller.getEvent()).isSameAs(event);
    }

    @Test
    void getAllSellers_returnsFromRepository() {
        List<SellerDetail> data = List.of(
                SellerDetail.builder().name("A").build(),
                SellerDetail.builder().name("B").build()
        );
        when(eventContext.currentId()).thenReturn(1L);
        when(repository.findByEventIdOrderByIdAsc(1L)).thenReturn(data);

        List<SellerDetail> result = service.getAllSellers();

        assertThat(result).isSameAs(data);
        verify(repository, times(1)).findByEventIdOrderByIdAsc(1L);
    }

    @Test
    void createSeller_buildsAndSavesEntity_withAllProvidedFields() {
        ArgumentCaptor<SellerDetail> captor = ArgumentCaptor.forClass(SellerDetail.class);
        when(eventContext.requireEditable()).thenReturn(event);

        service.createSeller("Bob", "456 Avenue", "bob@example.com", "+200000000", "Partner");

        verify(repository, times(1)).save(captor.capture());
        SellerDetail saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Bob");
        assertThat(saved.getAddress()).isEqualTo("456 Avenue");
        assertThat(saved.getEmail()).isEqualTo("bob@example.com");
        assertThat(saved.getPhone()).isEqualTo("+200000000");
        assertThat(saved.getDescription()).isEqualTo("Partner");
        assertThat(saved.getEvent()).isSameAs(event);
    }

    @Test
    void delete_delegatesToRepository() {
        SellerDetail toDelete = SellerDetail.builder().id(99L).name("Charlie").event(event).build();
        when(eventContext.requireEditable()).thenReturn(event);
        when(repository.findByIdAndEventId(99L, 1L)).thenReturn(Optional.of(toDelete));

        service.delete(toDelete);

        verify(repository, times(1)).delete(toDelete);
    }

    @Test
    void findByAccessToken_returnsEmpty_whenSellersEventArchived() {
        Event archived = Event.builder().id(2L).name("Old").status(EventStatus.ARCHIVED).build();
        SellerDetail seller = SellerDetail.builder().id(5L).accessToken("tok").event(archived).build();
        when(repository.findByAccessToken("tok")).thenReturn(Optional.of(seller));

        assertThat(service.findByAccessToken("tok")).isEmpty();
    }

    @Test
    void findByAccessToken_returnsSeller_whenSellersEventActive() {
        SellerDetail seller = SellerDetail.builder().id(5L).accessToken("tok").event(event).build();
        when(repository.findByAccessToken("tok")).thenReturn(Optional.of(seller));

        assertThat(service.findByAccessToken("tok")).containsSame(seller);
    }
}