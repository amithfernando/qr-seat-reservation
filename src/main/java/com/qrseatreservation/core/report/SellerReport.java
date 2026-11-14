package com.qrseatreservation.core.report;

import java.util.List;

/** Per-seller aggregate over their reserved tickets. */
public record SellerReport(String seller, List<TicketReportRow> tickets) {

    public long reserved() { return tickets.size(); }

    public long paid() { return tickets.stream().filter(TicketReportRow::isPaid).count(); }

    public long unpaid() { return reserved() - paid(); }

    public long checkedIn() { return tickets.stream().filter(TicketReportRow::isCheckedIn).count(); }

    public long notArrived() { return paid() - checkedIn(); }

    /** Revenue from paid (incl. checked-in) tickets. */
    public double revenue() { return tickets.stream().filter(TicketReportRow::isPaid).mapToDouble(TicketReportRow::price).sum(); }

    public List<String> paidTicketNos() {
        return tickets.stream().filter(TicketReportRow::isPaid).map(TicketReportRow::ticketNo).sorted().toList();
    }

    public List<String> checkedInTicketNos() {
        return tickets.stream().filter(TicketReportRow::isCheckedIn).map(TicketReportRow::ticketNo).sorted().toList();
    }
}
