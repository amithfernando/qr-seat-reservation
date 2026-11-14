package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventContext;

import com.qrseatreservation.core.dto.ReportMetrics;
import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.TicketCategory;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.SeatReservationRepository.RevenueBreakdown;
import com.qrseatreservation.core.repository.TicketCategoryRepository;
import com.qrseatreservation.core.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    
    private final TicketRepository ticketRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final SeatDetailRepository seatDetailRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventContext eventContext;
    
    /**
     * Get comprehensive report metrics without loading heavy data
     * Uses COUNT queries only for optimal performance
     */
    @Transactional(readOnly = true)
    public ReportMetrics getReportMetrics() {
        log.info("Generating report metrics");
        Long eventId = eventContext.currentId();
        
        // Seat metrics - primary focus
        long totalSeats = seatDetailRepository.countByEvent(eventId);
        long availableSeats = seatDetailRepository.countByStatus(eventId, SeatStatus.AVAILABLE);
        long reservedSeats = seatDetailRepository.countByStatus(eventId, SeatStatus.RESERVED);
        long checkedInSeats = seatDetailRepository.countByStatus(eventId, SeatStatus.CHECKED_IN);
        long unavailableSeats = seatDetailRepository.countByStatus(eventId, SeatStatus.UNAVAILABLE);
        long occupiedSeats = reservedSeats + checkedInSeats;
        
        // Reservation metrics - only checked-in count
        long paidReservations = seatReservationRepository.countByReservationStatus(eventId, ReservationStatus.CHECKED_IN);
        
        // Calculate percentages
        double seatOccupancy = totalSeats > 0 ? (occupiedSeats * 100.0 / totalSeats) : 0;
        
        // Build price map: category name → price
        Map<String, Double> categoryPriceByName = ticketCategoryRepository.findAllByEventOrdered(eventId).stream()
                .filter(tc -> tc.getPrice() != null)
                .collect(Collectors.toMap(TicketCategory::getName, TicketCategory::getPrice, (a, b) -> a));

        long complimentaryCheckedIn = seatReservationRepository.countByReservationStatusAndTicketType(
                eventId,
                ReservationStatus.CHECKED_IN,
                TicketType.INVITATION
        );

        var revenueBreakdowns = seatReservationRepository.summarizeRevenueByStatusExcludingTicketType(
                eventId,
                ReservationStatus.CHECKED_IN,
                TicketType.INVITATION
        );
        
        // Total revenue — paying tickets only (FULL + HALF)
        Double totalRevenue = revenueBreakdowns.stream()
                .mapToDouble(row -> resolveTicketPrice(row, categoryPriceByName) * row.getTicketCount())
                .sum();
        
        // Total tickets sold (paying, checked-in)
        Long totalTicketsSold = revenueBreakdowns.stream()
                .mapToLong(RevenueBreakdown::getTicketCount)
                .sum();
        
        // Average ticket price
        long pricedTicketCount = revenueBreakdowns.stream()
                .filter(row -> resolveTicketPrice(row, categoryPriceByName) > 0)
                .mapToLong(RevenueBreakdown::getTicketCount)
                .sum();
        Double averageTicketPrice = pricedTicketCount > 0 ? totalRevenue / pricedTicketCount : 0.0;
        
        // Ticket count by type (paying tickets only)
        Map<String, Long> ticketCountByCategory = revenueBreakdowns.stream()
                .filter(row -> row.getTicketType() != null)
                .collect(Collectors.groupingBy(
                        row -> row.getTicketType().name(),
                        Collectors.summingLong(RevenueBreakdown::getTicketCount)
                ));
        
        // Revenue by type (paying tickets only)
        Map<String, Double> revenueByCategory = revenueBreakdowns.stream()
                .filter(row -> row.getTicketType() != null)
                .collect(Collectors.groupingBy(
                        row -> row.getTicketType().name(),
                        Collectors.summingDouble(row -> resolveTicketPrice(row, categoryPriceByName) * row.getTicketCount())
                ));
        
        ReportMetrics metrics = ReportMetrics.builder()
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .occupiedSeats(occupiedSeats)
                .unavailableSeats(unavailableSeats)
                .paidReservations(paidReservations)
                .seatOccupancyPercentage(seatOccupancy)
                .totalRevenue(totalRevenue)
                .averageTicketPrice(averageTicketPrice)
                .totalTicketsSold(totalTicketsSold)
                .ticketCountByCategory(ticketCountByCategory)
                .revenueByCategory(revenueByCategory)
                .complimentaryCheckedIn(complimentaryCheckedIn)
                .build();
        
        log.info("Report metrics generated: {} total seats, {} paid reservations, {} unavailable, revenue: €{}", 
                totalSeats, paidReservations, unavailableSeats, String.format("%.2f", totalRevenue));
        
        return metrics;
    }

        private double resolveTicketPrice(RevenueBreakdown row, Map<String, Double> categoryPriceByName) {
                if (row.getTicketPrice() != null) return row.getTicketPrice();
                if (row.getCategoryPrice() != null) return row.getCategoryPrice();
                if (row.getTicketType() != null) return categoryPriceByName.getOrDefault(row.getTicketType().name(), 0.0);
                return 0.0;
        }
}
