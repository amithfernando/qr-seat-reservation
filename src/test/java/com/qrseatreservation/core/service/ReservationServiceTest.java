package com.qrseatreservation.core.service;


import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.model.Ticket;
import com.qrseatreservation.core.repository.ReservationDetailRepository;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private EventContext eventContext;

    private ReservationService reservationService;

    private final Event event = Event.builder().id(1L).name("E").status(EventStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationDetailRepository,
                seatReservationRepository,
                seatDetailRepository,
                ticketService,
                ticketCategoryRepository,
                eventContext
        );
        lenient().when(eventContext.currentId()).thenReturn(1L);
        lenient().when(eventContext.requireEditable()).thenReturn(event);
        lenient().when(eventContext.requireActive()).thenReturn(event);
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

        when(seatDetailRepository.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(seatDetail));
        when(ticketService.getTicketNumber(1L)).thenReturn("T-0001");

        // Act
        reservationService.saveReservation(rd);

        // Assert
        verify(ticketService, times(1)).getTicketNumber(1L);
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
        assertThat(rd.getEvent()).isSameAs(event);
    }

    @Test
    void getAllReservations_delegatesToRepository() {
        List<ReservationDetail> expected = List.of(ReservationDetail.builder().build());
        when(reservationDetailRepository.findAllWithDetails(1L)).thenReturn(expected);

        List<ReservationDetail> actual = reservationService.getAllReservations();

        assertThat(actual).isSameAs(expected);
        verify(reservationDetailRepository, times(1)).findAllWithDetails(1L);
    }

    @Test
    void deleteReservation_deletesSeatReservations_setsSeatAvailable_andDeletesReservation() {
        SeatDetail sd = new SeatDetail();
        setSeatDetailId(sd, 5L);
        sd.setSeatStatus(SeatStatus.RESERVED);
        SeatReservation sr = SeatReservation.builder().seatDetail(sd).build();

        ReservationDetail detached = ReservationDetail.builder().id(1L).seatReservations(Set.of(sr)).build();
        ReservationDetail managed = ReservationDetail.builder().id(1L)
                .seatReservations(new HashSet<>(Set.of(sr))).build();
        when(reservationDetailRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(managed));

        reservationService.deleteReservation(detached);

        InOrder order = inOrder(reservationDetailRepository, seatReservationRepository);
        order.verify(reservationDetailRepository).delete(managed);
        order.verify(reservationDetailRepository).flush();
        order.verify(seatReservationRepository).delete(sr);
        assertThat(managed.getSeatReservations()).isEmpty();
        verify(seatDetailRepository, times(1)).save(argThat(saved ->
                saved.getSeatStatus() == SeatStatus.AVAILABLE && Objects.equals(getSeatDetailId(saved), 5L)
        ));
        assertThat(sd.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void deleteReservation_releasesTicketsBackToPool_exceptCheckedIn() {
        SeatReservation paid = SeatReservation.builder().seatDetail(new SeatDetail()).ticketNo("A1")
                .reservationStatus(ReservationStatus.PAID).build();
        SeatReservation unpaid = SeatReservation.builder().seatDetail(new SeatDetail()).ticketNo("A2")
                .reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        SeatReservation entered = SeatReservation.builder().seatDetail(new SeatDetail()).ticketNo("A3")
                .reservationStatus(ReservationStatus.CHECKED_IN).build();
        ReservationDetail managed = ReservationDetail.builder().id(7L)
                .seatReservations(new HashSet<>(Set.of(paid, unpaid, entered))).build();
        when(reservationDetailRepository.findByIdAndEventId(7L, 1L)).thenReturn(Optional.of(managed));

        reservationService.deleteReservation(ReservationDetail.builder().id(7L).build());

        verify(ticketService).releaseTickets(eq(1L), argThat((java.util.Collection<String> nos) ->
                nos.size() == 2 && nos.containsAll(List.of("A1", "A2")) && !nos.contains("A3")));
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

        when(ticketService.generateOnTheFly("A1", "")).thenReturn(new byte[]{1, 2});
        when(ticketService.generateOnTheFly("A2", "")).thenReturn(new byte[]{3, 4});

        byte[] zipBytes = new byte[]{9, 9, 9};
        when(ticketService.createZipFromImages(anyList())).thenReturn(zipBytes);

        byte[] result = reservationService.getTicketImageZip(rd);

        assertThat(result).isSameAs(zipBytes);
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(ticketService, times(1)).createZipFromImages(captor.capture());
        assertThat(captor.getValue()).extracting(Ticket::getTicketNo).containsExactlyInAnyOrder("A1", "A2");
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
                .event(event)
                .build();

        reservationService.setPaymentDone(rd);

        assertThat(sr.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        verify(seatReservationRepository, times(1)).save(sr);
        verify(reservationDetailRepository, times(1)).save(rd);
    }

    @Test
    void setPaymentDone_doesNotResetCheckedInTickets() {
        SeatReservation checkedIn = SeatReservation.builder().id(1L).reservationStatus(ReservationStatus.CHECKED_IN).build();
        SeatReservation pending = SeatReservation.builder().id(2L).reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        ReservationDetail rd = ReservationDetail.builder().seatReservations(Set.of(checkedIn, pending)).event(event).build();

        reservationService.setPaymentDone(rd);

        assertThat(checkedIn.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(pending.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
    }

    @Test
    void setTicketPaid_paysOneTicket_reservationStaysPendingUntilAllPaid() {
        SeatReservation a = SeatReservation.builder().id(1L).ticketNo("DJ0001").reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        SeatReservation b = SeatReservation.builder().id(2L).ticketNo("DJ0002").reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        ReservationDetail rd = ReservationDetail.builder().id(9L).seatReservations(Set.of(a, b))
                .reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        when(seatReservationRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(a));
        when(seatReservationRepository.findByIdAndEventId(2L, 1L)).thenReturn(Optional.of(b));
        when(reservationDetailRepository.findBySeatReservationId(eq(1L), anyLong())).thenReturn(Optional.of(rd));
        when(reservationDetailRepository.findByIdWithDetails(1L, 9L)).thenReturn(Optional.of(rd));

        reservationService.setTicketPaid(1L, true);
        assertThat(a.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);

        reservationService.setTicketPaid(2L, true);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAID);

        reservationService.setTicketPaid(1L, false);
        assertThat(a.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);
        assertThat(rd.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);
    }

    @Test
    void setTicketPaid_rejectsCheckedInTicket() {
        SeatReservation a = SeatReservation.builder().id(1L).ticketNo("DJ0001").reservationStatus(ReservationStatus.CHECKED_IN).build();
        when(seatReservationRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(a));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> reservationService.setTicketPaid(1L, false));
        assertThat(a.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
    }

    @Test
    void checkInByTicketNo_refusesUnpaidTicket() {
        SeatDetail sd = new SeatDetail();
        sd.setSeatStatus(SeatStatus.RESERVED);
        SeatReservation sr = SeatReservation.builder().ticketNo("DJ0001").seatDetail(sd)
                .reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "DJ0001"))
                .thenReturn(List.of(ReservationDetail.builder().seatReservations(Set.of(sr)).build()));

        assertThat(reservationService.checkInByTicketNo("DJ0001")).isFalse();
        assertThat(sr.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);
        verify(seatReservationRepository, never()).save(any());
    }

    @Test
    void undoCheckIn_revertsTicketToPaidAndSeatToReserved() {
        SeatDetail sd = new SeatDetail();
        sd.setSeatStatus(SeatStatus.CHECKED_IN);
        SeatReservation sr = SeatReservation.builder().ticketNo("DJ0002").seatDetail(sd)
                .reservationStatus(ReservationStatus.CHECKED_IN).build();
        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "DJ0002"))
                .thenReturn(List.of(ReservationDetail.builder().seatReservations(Set.of(sr)).build()));

        assertThat(reservationService.undoCheckIn("DJ0002")).isTrue();
        assertThat(sr.getReservationStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(sd.getSeatStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(reservationService.undoCheckIn("DJ0002")).isFalse();
    }

    @Test
    void checkInAllPaid_checksInOnlyPaidTickets() {
        SeatReservation paid1 = SeatReservation.builder().id(1L).seatDetail(new SeatDetail()).reservationStatus(ReservationStatus.PAID).build();
        SeatReservation paid2 = SeatReservation.builder().id(2L).seatDetail(new SeatDetail()).reservationStatus(ReservationStatus.PAID).build();
        SeatReservation unpaid = SeatReservation.builder().id(3L).reservationStatus(ReservationStatus.PAYMENT_PENDING).build();
        ReservationDetail rd = ReservationDetail.builder().id(5L).seatReservations(Set.of(paid1, paid2, unpaid)).build();
        when(reservationDetailRepository.findByIdWithDetails(1L, 5L)).thenReturn(Optional.of(rd));

        assertThat(reservationService.checkInAllPaid(5L)).isEqualTo(2);
        assertThat(paid1.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(paid2.getSeatDetail().getSeatStatus()).isEqualTo(SeatStatus.CHECKED_IN);
        assertThat(unpaid.getReservationStatus()).isEqualTo(ReservationStatus.PAYMENT_PENDING);
    }

    @Test
    void findReservationByTicketNo_returnsMatchingReservation() {
        SeatReservation sr1 = SeatReservation.builder().ticketNo("X1").build();
        SeatReservation sr2 = SeatReservation.builder().ticketNo("X2").build();
        ReservationDetail r1 = ReservationDetail.builder().seatReservations(Set.of(sr1)).build();
        ReservationDetail r2 = ReservationDetail.builder().seatReservations(Set.of(sr2)).build();

        lenient().when(reservationDetailRepository.findByTicketNoWithDetails(1L, "X1")).thenReturn(List.of(r1));
        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "X2")).thenReturn(List.of(r2));

        ReservationDetail found = reservationService.findReservationByTicketNo("X2");
        assertThat(found).isSameAs(r2);
    }

    @Test
    void findReservationByTicketNo_handlesNullBlank_andNoMatch() {
        assertThat(reservationService.findReservationByTicketNo(null)).isNull();
        assertThat(reservationService.findReservationByTicketNo("  ")).isNull();

        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "NOPE")).thenReturn(List.of());
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

        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "TICK-1")).thenReturn(List.of(r));

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

        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "TICK-2")).thenReturn(List.of(r));

        boolean result = reservationService.checkInByTicketNo("TICK-2");

        assertThat(result).isFalse();
        verify(seatReservationRepository, never()).save(any());
        verify(seatDetailRepository, never()).save(any());
    }

    @Test
    void checkInByTicketNo_notFoundOrInvalid_returnsFalse() {
        when(reservationDetailRepository.findByTicketNoWithDetails(1L, "UNKNOWN")).thenReturn(List.of());

        assertThat(reservationService.checkInByTicketNo(null)).isFalse();
        assertThat(reservationService.checkInByTicketNo(" ")).isFalse();
        assertThat(reservationService.checkInByTicketNo("UNKNOWN")).isFalse();
    }

    @Test
    void checkInByTicketNo_throws_whenEventNotActive_andSavesNothing() {
        when(eventContext.requireActive()).thenThrow(new EventAccessException("Event is ARCHIVED"));

        assertThatThrownBy(() -> reservationService.checkInByTicketNo("TICK-1"))
                .isInstanceOf(EventAccessException.class);
        verify(reservationDetailRepository, never()).findByTicketNoWithDetails(anyLong(), anyString());
        verify(seatReservationRepository, never()).save(any());
        verify(seatDetailRepository, never()).save(any());
    }

    @Test
    void saveReservation_rejectsSellerOfOtherEvent() {
        Event other = Event.builder().id(2L).name("Other").status(EventStatus.ACTIVE).build();
        SellerDetail seller = SellerDetail.builder().id(3L).event(other).build();
        ReservationDetail rd = ReservationDetail.builder().sellerDetail(seller)
                .seatReservations(Set.of(SeatReservation.builder().seatDetail(seatDetailRef(10L)).build())).build();

        assertThatThrownBy(() -> reservationService.saveReservation(rd)).isInstanceOf(EventAccessException.class);
        verifyNoInteractions(ticketService);
        verify(reservationDetailRepository, never()).save(any());
    }

    @Test
    void updateBuyerName_updatesOwnTicket_inSellersEvent() {
        SellerDetail seller = SellerDetail.builder().id(3L).event(event).build();
        SeatReservation sr = SeatReservation.builder().id(20L).ticketNo("A").build();
        ReservationDetail rd = ReservationDetail.builder().sellerDetail(seller).event(event)
                .seatReservations(Set.of(sr)).build();
        when(reservationDetailRepository.findAllWithDetailsBySeller(1L, 3L)).thenReturn(List.of(rd));

        assertThat(reservationService.updateBuyerName(seller, 20L, "  Bob ")).isTrue();
        assertThat(sr.getBuyerName()).isEqualTo("Bob");
        verify(seatReservationRepository).save(sr);
        assertThat(reservationService.updateBuyerName(seller, 99L, "X")).isFalse();
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
