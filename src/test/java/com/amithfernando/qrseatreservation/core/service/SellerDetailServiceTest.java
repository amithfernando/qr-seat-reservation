package com.amithfernando.qrseatreservation.core.service;


import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.repsitory.SellerDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerDetailServiceTest {

    @Mock
    private SellerDetailRepository repository;

    private SellerDetailService service;

    @BeforeEach
    void setUp() {
        service = new SellerDetailService(repository);
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

        service.saveSeller(seller);

        verify(repository, times(1)).save(seller);
    }

    @Test
    void getAllSellers_returnsFromRepository() {
        List<SellerDetail> data = List.of(
                SellerDetail.builder().name("A").build(),
                SellerDetail.builder().name("B").build()
        );
        when(repository.findAll()).thenReturn(data);

        List<SellerDetail> result = service.getAllSellers();

        assertThat(result).isSameAs(data);
        verify(repository, times(1)).findAll();
    }

    @Test
    void createSeller_buildsAndSavesEntity_withAllProvidedFields() {
        ArgumentCaptor<SellerDetail> captor = ArgumentCaptor.forClass(SellerDetail.class);

        service.createSeller("Bob", "456 Avenue", "bob@example.com", "+200000000", "Partner");

        verify(repository, times(1)).save(captor.capture());
        SellerDetail saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Bob");
        assertThat(saved.getAddress()).isEqualTo("456 Avenue");
        assertThat(saved.getEmail()).isEqualTo("bob@example.com");
        assertThat(saved.getPhone()).isEqualTo("+200000000");
        assertThat(saved.getDescription()).isEqualTo("Partner");
    }

    @Test
    void delete_delegatesToRepository() {
        SellerDetail toDelete = SellerDetail.builder().id(99L).name("Charlie").build();

        service.delete(toDelete);

        verify(repository, times(1)).delete(toDelete);
    }
}