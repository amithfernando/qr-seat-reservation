package com.qrseatreservation.core.service;

import com.qrseatreservation.core.dto.ReportMetrics;
import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.TicketCategory;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.TicketCategoryRepository;
import com.qrseatreservation.core.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private SeatDetailRepository seatDetailRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private EventContext eventContext;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                ticketRepository,
                seatReservationRepository,
                seatDetailRepository,
                ticketCategoryRepository,
                eventContext
        );
    }

    @Test
    void getReportMetrics_usesAggregatedRevenueRows_withoutLoadingReservations() {
        when(eventContext.currentId()).thenReturn(1L);
        when(seatDetailRepository.countByEvent(1L)).thenReturn(10L);
        when(seatDetailRepository.countByStatus(1L, SeatStatus.AVAILABLE)).thenReturn(4L);
        when(seatDetailRepository.countByStatus(1L, SeatStatus.RESERVED)).thenReturn(3L);
        when(seatDetailRepository.countByStatus(1L, SeatStatus.CHECKED_IN)).thenReturn(2L);
        when(seatDetailRepository.countByStatus(1L, SeatStatus.UNAVAILABLE)).thenReturn(1L);

        when(seatReservationRepository.countByReservationStatus(1L, ReservationStatus.CHECKED_IN)).thenReturn(7L);
        when(seatReservationRepository.countByReservationStatusAndTicketType(
                1L,
                ReservationStatus.CHECKED_IN,
                TicketType.INVITATION
        )).thenReturn(1L);
        when(ticketCategoryRepository.findAllByEventOrdered(1L)).thenReturn(List.of(
                TicketCategory.builder().name("FULL").price(100.0).build(),
                TicketCategory.builder().name("HALF").price(50.0).build()
        ));
        when(seatReservationRepository.summarizeRevenueByStatusExcludingTicketType(
                1L,
                ReservationStatus.CHECKED_IN,
                TicketType.INVITATION
        )).thenReturn(List.of(
                row(TicketType.FULL, 120.0, null, 2L),
                row(TicketType.HALF, null, null, 3L),
                row(null, 10.0, null, 1L)
        ));

        ReportMetrics metrics = reportService.getReportMetrics();

        assertThat(metrics.getTotalSeats()).isEqualTo(10L);
        assertThat(metrics.getAvailableSeats()).isEqualTo(4L);
        assertThat(metrics.getOccupiedSeats()).isEqualTo(5L);
        assertThat(metrics.getUnavailableSeats()).isEqualTo(1L);
        assertThat(metrics.getPaidReservations()).isEqualTo(7L);
        assertThat(metrics.getSeatOccupancyPercentage()).isEqualTo(50.0);
        assertThat(metrics.getComplimentaryCheckedIn()).isEqualTo(1L);
        assertThat(metrics.getTotalTicketsSold()).isEqualTo(6L);
        assertThat(metrics.getTotalRevenue()).isEqualTo(400.0);
        assertThat(metrics.getAverageTicketPrice()).isEqualTo(400.0 / 6.0);
        assertThat(metrics.getTicketCountByCategory()).containsEntry("FULL", 2L).containsEntry("HALF", 3L);
        assertThat(metrics.getRevenueByCategory()).containsEntry("FULL", 240.0).containsEntry("HALF", 150.0);

        verify(seatReservationRepository, never()).findAllWithSeatDetails(anyLong());
    }

    private SeatReservationRepository.RevenueBreakdown row(
            TicketType ticketType,
            Double ticketPrice,
            Double categoryPrice,
            Long ticketCount
    ) {
        return new SeatReservationRepository.RevenueBreakdown() {
            @Override
            public TicketType getTicketType() {
                return ticketType;
            }

            @Override
            public Double getTicketPrice() {
                return ticketPrice;
            }

            @Override
            public Double getCategoryPrice() {
                return categoryPrice;
            }

            @Override
            public Long getTicketCount() {
                return ticketCount;
            }
        };
    }
}