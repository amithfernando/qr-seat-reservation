package com.amithfernando.qrseatreservation.core.service;


import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;
import com.amithfernando.qrseatreservation.core.enums.SeatStatus;
import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.amithfernando.qrseatreservation.core.model.Ticket;
import com.amithfernando.qrseatreservation.core.repsitory.ReservationDetailRepository;
import com.amithfernando.qrseatreservation.core.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.core.repsitory.SeatReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationDetailRepository reservationDetailRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private SeatDetailRepository seatDetailRepository;
    @Mock
    private TicketService ticketService;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationDetailRepository,
                seatReservationRepository,
                seatDetailRepository,
                ticketService
        );
    }

    @Test
    void saveReservation_setsTicketNo_updatesSeatStatus_andPersistsAll() {
        // Arrange
        SeatDetail seatDetail = new SeatDetail();
        setSeatDetailId(seatDetail, 10L);
        seatDetail.setSeatStatus(SeatStatus.AVAILABLE);

        SeatReservation sr = SeatReservation.builder()
                .seatDetail(seatDetailRef(10L)) // reference by id, repository should load the entity
                .build();

        ReservationDetail rd = ReservationDetail.builder()
                .seatReservations(Set.of(sr))
                .build();

        when(seatDetailRepository.findById(10L)).thenReturn(Optional.of(seatDetail));
        when(ticketService.getTicketNumber()).thenReturn("T-0001");

        // Act
        reservationService.saveReservation(rd);

        // Assert
        verify(ticketService, times(1)).getTicketNumber();
        verify(seatReservationRepository, times(1)).save(any(SeatReservation.class));
        verify(seatDetailRepository, times(1)).save(argThat(sd ->
                sd.getSeatStatus() == SeatStatus.RESERVED && Objects.equals(getSeatDetailId(sd), 10L)
        ));
        verify(reservationDetailRepository, times(1)).save(argThat(saved ->
                saved.getReservationStatus() == ReservationStatus.PAYMENT_PENDING
                        && saved.getReferenceNo() != null && !saved.getReferenceNo().isBlank()
        ));
        assertThat(sr.getTicketNo()).isEqualTo("T-0001");
        assertThat(seatDetail.getSeatStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);
        assertThat(rd.getReferenceNo()).isNotBlank();
    }

    @Test
    void getAllReservations_delegatesToRepository() {
        List<ReservationDetail> expected = List.of(ReservationDetail.builder().build());
        when(reservationDetailRepository.findAllWithDetails()).thenReturn(expected);

        List<ReservationDetail> actual = reservationService.getAllReservations();

        assertThat(actual).isSameAs(expected);
        verify(reservationDetailRepository, times(1)).findAllWithDetails();
    }

    @Test
    void deleteReservation_deletesSeatReservations_setsSeatAvailable_andDeletesReservation() {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, 5L);
        SeatReservation sr = SeatReservation.builder().seatDetail(seatDetailRef(5L)).build();

        ReservationDetail rd = ReservationDetail.builder()
                .seatReservations(Set.of(sr))
                .build();

        when(seatDetailRepository.findById(5L)).thenReturn(Optional.of(sd));

        reservationService.deleteReservation(rd);

        verify(seatReservationRepository, times(1)).delete(sr);
        verify(seatDetailRepository, times(1)).save(argThat(saved ->
                saved.getSeatStatus() == SeatStatus.AVAILABLE && Objects.equals(getSeatDetailId(saved), 5L)
        ));
        verify(reservationDetailRepository, times(1)).delete(rd);
        assertThat(sd.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void getTicketImageZip_returnsNullWhenReservationNull() throws IOException {
        byte[] result = reservationService.getTicketImageZip(null);
        assertThat(result).isNull();
        verifyNoInteractions(ticketService);
    }

    @Test
    void getTicketImageZip_collectsTickets_andDelegatesToZip() throws IOException {
        SeatReservation sr1 = SeatReservation.builder().ticketNo("A1").build();
        SeatReservation sr2 = SeatReservation.builder().ticketNo("A2").build();

        ReservationDetail rd = ReservationDetail.builder()
                .seatReservations(Set.of(sr1, sr2))
                .build();

        Ticket t1 = new Ticket("A1", null, new byte[]{1, 2});
        Ticket t2 = new Ticket("A2", null, new byte[]{3, 4});

        when(ticketService.findByTicketNo("A1")).thenReturn(t1);
        when(ticketService.findByTicketNo("A2")).thenReturn(t2);

        byte[] zipBytes = new byte[]{9, 9, 9};
        when(ticketService.createZipFromImages(anyList())).thenReturn(zipBytes);

        byte[] result = reservationService.getTicketImageZip(rd);

        assertThat(result).isSameAs(zipBytes);
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(ticketService, times(1)).createZipFromImages(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void setPaymentDone_setsReservationAndSeatsToPaid_andPersists() {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, 7L);
        SeatReservation sr = SeatReservation.builder()
                .seatDetail(seatDetailRef(7L))
                .reservationStatus(ReservationStatus.PAYMENT_PENDING)
                .build();

        ReservationDetail rd = ReservationDetail.builder()
                .seatReservations(Set.of(sr))
                .reservationStatus(ReservationStatus.PAYMENT_PENDING)
                .build();

        when(seatDetailRepository.findById(7L)).thenReturn(Optional.of(sd));

        reservationService.setPaymentDone(rd);

        assertThat(sr.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        verify(seatReservationRepository, times(1)).save(sr);
        verify(reservationDetailRepository, times(1)).save(rd);
    }

    @Test
    void findReservationByTicketNo_returnsMatchingReservation() {
        SeatReservation sr1 = SeatReservation.builder().ticketNo("X1").build();
        SeatReservation sr2 = SeatReservation.builder().ticketNo("X2").build();
        ReservationDetail r1 = ReservationDetail.builder().seatReservations(Set.of(sr1)).build();
        ReservationDetail r2 = ReservationDetail.builder().seatReservations(Set.of(sr2)).build();

        when(reservationDetailRepository.findAllWithDetails()).thenReturn(List.of(r1, r2));

        ReservationDetail found = reservationService.findReservationByTicketNo("X2");
        assertThat(found).isSameAs(r2);
    }

    @Test
    void findReservationByTicketNo_handlesNullBlank_andNoMatch() {
        assertThat(reservationService.findReservationByTicketNo(null)).isNull();
        assertThat(reservationService.findReservationByTicketNo("  ")).isNull();

        when(reservationDetailRepository.findAllWithDetails()).thenReturn(List.of());
        assertThat(reservationService.findReservationByTicketNo("NOPE")).isNull();
    }

    @Test
    void checkInByTicketNo_success_updatesSeatReservationAndSeat_andReturnsTrue() {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, 11L);
        sd.setSeatStatus(SeatStatus.RESERVED);
        SeatReservation target = SeatReservation.builder()
                .seatDetail(sd)
                .ticketNo("TICK-1")
                .reservationStatus(ReservationStatus.PAID)
                .build();

        ReservationDetail r = ReservationDetail.builder()
                .seatReservations(Set.of(target))
                .build();

        when(reservationDetailRepository.findAllWithDetails()).thenReturn(List.of(r));

        boolean result = reservationService.checkInByTicketNo("TICK-1");

        assertThat(result).isTrue();
        assertThat(target.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(sd.getSeatStatus()).isEqualTo(SeatStatus.CHECKED_IN);
        verify(seatReservationRepository, times(1)).save(target);
        verify(seatDetailRepository, times(1)).save(sd);
    }

    @Test
    void checkInByTicketNo_alreadyCheckedIn_returnsFalse_andDoesNotSave() {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, 12L);
        sd.setSeatStatus(SeatStatus.CHECKED_IN);
        SeatReservation target = SeatReservation.builder()
                .seatDetail(sd)
                .ticketNo("TICK-2")
                .reservationStatus(ReservationStatus.CHECKED_IN)
                .build();

        ReservationDetail r = ReservationDetail.builder()
                .seatReservations(Set.of(target))
                .build();

        when(reservationDetailRepository.findAllWithDetails()).thenReturn(List.of(r));

        boolean result = reservationService.checkInByTicketNo("TICK-2");

        assertThat(result).isFalse();
        verify(seatReservationRepository, never()).save(any());
        verify(seatDetailRepository, never()).save(any());
    }

    @Test
    void checkInByTicketNo_notFoundOrInvalid_returnsFalse() {
        when(reservationDetailRepository.findAllWithDetails()).thenReturn(List.of());

        assertThat(reservationService.checkInByTicketNo(null)).isFalse();
        assertThat(reservationService.checkInByTicketNo(" ")).isFalse();
        assertThat(reservationService.checkInByTicketNo("UNKNOWN")).isFalse();
    }

    // Helpers

    private SeatDetail seatDetailRef(Long id) {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, id);
        return sd;
    }

    // Use reflection to set id if field is private in your entity (common in JPA)
    private void setSeatDetailId(SeatDetail sd, Long id) {
        try {
            var f = SeatDetail.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(sd, id);
        } catch (NoSuchFieldException e) {
            // If SeatDetail exposes a setter, use it; otherwise ignore
            // Replace with sd.setId(id) if available in your entity
        } catch (IllegalAccessException ignored) {
        }
    }

    private Long getSeatDetailId(SeatDetail sd) {
        try {
            var f = SeatDetail.class.getDeclaredField("id");
            f.setAccessible(true);
            return (Long) f.get(sd);
        } catch (Exception e) {
            return null;
        }
    }
}
