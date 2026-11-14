package com.qrseatreservation.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetrics {
    
    // Seat metrics (primary focus)
    private long totalSeats;
    private long occupiedSeats;
    private long availableSeats;
    private long unavailableSeats;
    
    // Reservation metrics
    private long paidReservations;
    
    // Calculated percentages
    private double seatOccupancyPercentage;
    
    // Financial metrics
    private Double totalRevenue;
    private Double averageTicketPrice;
    private Long totalTicketsSold;
    
    // Category breakdown
    private Map<String, Long> ticketCountByCategory;
    private Map<String, Double> revenueByCategory;

    // Complimentary tickets (INVITATION type — excluded from revenue)
    private long complimentaryCheckedIn;
}
