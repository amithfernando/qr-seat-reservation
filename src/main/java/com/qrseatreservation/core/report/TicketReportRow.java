package com.qrseatreservation.core.report;

import com.qrseatreservation.core.enums.ReservationStatus;

import java.time.LocalDateTime;

/** One reserved ticket, flattened for reports and exports. */
public record TicketReportRow(
        String seller,
        String referenceNo,
        String ticketNo,
        String table,
        String seat,
        String category,
        double price,
        ReservationStatus status,
        String buyerName,
        LocalDateTime reservedAt,
        LocalDateTime updatedAt) {

    public boolean isPaid() {
        return status == ReservationStatus.PAID || status == ReservationStatus.CHECKED_IN;
    }

    public boolean isCheckedIn() {
        return status == ReservationStatus.CHECKED_IN;
    }

    /** Check-in time: the last update of a checked-in ticket. */
    public LocalDateTime checkedInAt() {
        return isCheckedIn() ? updatedAt : null;
    }

    public String statusLabel() {
        if (status == null) return "";
        return switch (status) {
            case PAID -> "Paid";
            case CHECKED_IN -> "Checked in";
            case PAYMENT_PENDING -> "Unpaid";
        };
    }
}
