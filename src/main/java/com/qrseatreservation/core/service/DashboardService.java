package com.qrseatreservation.core.service;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.enums.TicketStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** Aggregates everything the Home dashboard shows for the current event in one pass. */
@Service
public class DashboardService {

    public record SeatStats(long total, long available, long reserved, long checkedIn, long unavailable) {
        public long sellable() { return total - unavailable; }
        public double occupancyPct() { return sellable() > 0 ? (reserved + checkedIn) * 100.0 / sellable() : 0; }
    }

    public record TicketStats(long generated, long assigned, long available) {}

    public record MoneyStats(double collected, double outstanding, long paidTickets, long pendingTickets,
                             long invitationTickets) {}

    public record CategoryStat(String name, long tickets, double revenue) {}

    public record SellerStat(String name, long tickets, double revenue) {}

    public record TableStat(Long id, String name, long available, long total, List<SeatStatus> seats) {}

    public record RecentReservation(Long id, String seller, int seats, String tables, ReservationStatus status,
                                    LocalDateTime createdAt) {}

    public record Snapshot(Event event, Long daysToGo, SeatStats seats, TicketStats tickets, MoneyStats money,
                           long reservations, long ticketsSold, long checkedInTickets, long expectedGuests,
                           long sellers, long tables, List<CategoryStat> categories, List<SellerStat> topSellers,
                           List<TableStat> tableStats, List<RecentReservation> recent) {
        public double checkInPct() { return expectedGuests > 0 ? checkedInTickets * 100.0 / expectedGuests : 0; }
    }

    private final EventContext eventContext;
    private final ReservationService reservationService;
    private final TableDetailService tableDetailService;
    private final SellerDetailService sellerDetailService;
    private final TicketService ticketService;

    public DashboardService(EventContext eventContext, ReservationService reservationService,
                            TableDetailService tableDetailService, SellerDetailService sellerDetailService,
                            TicketService ticketService) {
        this.eventContext = eventContext;
        this.reservationService = reservationService;
        this.tableDetailService = tableDetailService;
        this.sellerDetailService = sellerDetailService;
        this.ticketService = ticketService;
    }

    public Snapshot snapshot() {
        Event event = eventContext.current();
        List<TableDetail> tables = tableDetailService.getAllTables();
        List<ReservationDetail> reservations = reservationService.getAllReservations();

        long total = 0, available = 0, reserved = 0, checkedIn = 0, unavailable = 0;
        List<TableStat> tableStats = new ArrayList<>();
        for (TableDetail t : tables) {
            List<SeatDetail> seats = t.getSeatDetails() != null ? t.getSeatDetails() : List.of();
            long free = 0;
            List<SeatStatus> statuses = new ArrayList<>();
            for (SeatDetail s : seats.stream().sorted(Comparator.comparing(SeatDetail::getId, Comparator.nullsLast(Long::compareTo))).toList()) {
                SeatStatus st = s.getSeatStatus() != null ? s.getSeatStatus() : SeatStatus.AVAILABLE;
                statuses.add(st);
                total++;
                switch (st) {
                    case AVAILABLE -> { available++; free++; }
                    case RESERVED -> reserved++;
                    case CHECKED_IN -> checkedIn++;
                    case UNAVAILABLE -> unavailable++;
                }
            }
            tableStats.add(new TableStat(t.getId(), t.getTableName(), free, seats.size(), statuses));
        }

        double collected = 0, outstanding = 0;
        long paid = 0, pending = 0, invitations = 0, sold = 0, checkedInTickets = 0;
        Map<String, double[]> byCategory = new LinkedHashMap<>();
        Map<String, double[]> bySeller = new HashMap<>();
        for (ReservationDetail rd : reservations) {
            String seller = rd.getSellerDetail() != null && rd.getSellerDetail().getName() != null
                    ? rd.getSellerDetail().getName() : "Unknown";
            if (rd.getSeatReservations() == null) continue;
            for (SeatReservation sr : rd.getSeatReservations()) {
                if (sr == null) continue;
                sold++;
                boolean invitation = sr.getTicketType() == TicketType.INVITATION;
                double price = invitation ? 0 : price(sr);
                ReservationStatus st = sr.getReservationStatus();
                if (st == ReservationStatus.CHECKED_IN) checkedInTickets++;
                if (invitation) invitations++;
                if (st == ReservationStatus.PAID || st == ReservationStatus.CHECKED_IN) {
                    if (!invitation) { paid++; collected += price; }
                } else {
                    pending++;
                    outstanding += price;
                }
                String category = sr.getTicketCategory() != null ? sr.getTicketCategory().getName()
                        : sr.getTicketType() != null ? sr.getTicketType().name() : "Other";
                double[] c = byCategory.computeIfAbsent(category, k -> new double[2]);
                c[0]++;
                c[1] += price;
                double[] s = bySeller.computeIfAbsent(seller, k -> new double[2]);
                s[0]++;
                s[1] += price;
            }
        }

        List<CategoryStat> categories = byCategory.entrySet().stream()
                .map(e -> new CategoryStat(e.getKey(), (long) e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(CategoryStat::tickets).reversed())
                .toList();
        List<SellerStat> topSellers = bySeller.entrySet().stream()
                .map(e -> new SellerStat(e.getKey(), (long) e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(SellerStat::tickets).reversed().thenComparing(SellerStat::name))
                .limit(5)
                .toList();
        List<RecentReservation> recent = reservations.stream()
                .sorted(Comparator.comparing(ReservationDetail::getId).reversed())
                .limit(6)
                .map(DashboardService::toRecent)
                .toList();

        long generated = ticketService.countTickets();
        long assigned = ticketService.countTicketsByStatus(TicketStatus.USED);
        Long daysToGo = event.getEventDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), event.getEventDate()) : null;

        return new Snapshot(event, daysToGo,
                new SeatStats(total, available, reserved, checkedIn, unavailable),
                new TicketStats(generated, assigned, Math.max(0, generated - assigned)),
                new MoneyStats(collected, outstanding, paid, pending, invitations),
                reservations.size(), sold, checkedInTickets, sold - pending,
                sellerDetailService.getAllSellers().size(), tables.size(),
                categories, topSellers, tableStats, recent);
    }

    private static double price(SeatReservation sr) {
        if (sr.getTicketPrice() != null) return sr.getTicketPrice();
        if (sr.getTicketCategory() != null && sr.getTicketCategory().getPrice() != null) return sr.getTicketCategory().getPrice();
        return 0;
    }

    private static RecentReservation toRecent(ReservationDetail rd) {
        Set<SeatReservation> seats = rd.getSeatReservations() != null ? rd.getSeatReservations() : Set.of();
        String tables = seats.stream()
                .map(sr -> sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                        ? sr.getSeatDetail().getTableDetail().getTableName() : null)
                .filter(Objects::nonNull).distinct().sorted().collect(Collectors.joining(", "));
        return new RecentReservation(rd.getId(),
                rd.getSellerDetail() != null ? rd.getSellerDetail().getName() : "Unknown",
                seats.size(), tables, rd.getReservationStatus(), rd.getCreatedAt());
    }
}
