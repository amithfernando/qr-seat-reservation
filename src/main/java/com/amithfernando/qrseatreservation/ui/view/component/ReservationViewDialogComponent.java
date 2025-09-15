package com.amithfernando.qrseatreservation.ui.view.component;

import com.amithfernando.qrseatreservation.core.enums.TicketType;
import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Objects;
import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;

public class ReservationViewDialogComponent extends VerticalLayout {

    private TextField referenceNo;
    private TextField seller;
    private TextField table;
    private TextField seats;
    private TextField description;

    private Span fullBadge;
    private Span halfBadge;
    private Span statusBadge; // Reservation status badge

    private ReservationDetail reservationDetail;
    private final Grid<SeatReservation> tableGrid = new Grid<>(SeatReservation.class, false);

    public ReservationViewDialogComponent() {
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        // Summary badges
        fullBadge = new Span("FULL: 0");
        fullBadge.getElement().getThemeList().add("badge contrast");
        halfBadge = new Span("HALF: 0");
        halfBadge.getElement().getThemeList().add("badge");
        statusBadge = new Span("Status: -");
        statusBadge.getElement().getThemeList().add("badge"); // theme updated dynamically

        HorizontalLayout badges = new HorizontalLayout(statusBadge, fullBadge, halfBadge);
        badges.setSpacing(true);
        badges.setPadding(false);

        // Read-only fields
        referenceNo = roField("Reference #");
        seller = roField("Seller");
        table = roField("Table");
        seats = roField("No. of Seats");
        description = roField("Description");

        // Details layout
        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        form.add(referenceNo, seller, table, seats, description);
        form.setColspan(description, 2);

        // Seats grid
        tableGrid.setWidthFull();
        tableGrid.setHeight("260px");
        tableGrid.addColumn(sr -> sr.getSeatDetail() != null ? sr.getSeatDetail().getSeatNo() : "")
                .setHeader("Seat No")
                .setAutoWidth(true)
                .setSortable(true);
        tableGrid.addColumn(sr -> {
            TicketType tt = sr.getTicketType();
            return tt != null ? tt.name() : "";
        }).setHeader("Ticket Type")
          .setAutoWidth(true)
          .setSortable(true);
        tableGrid.addColumn(sr -> {
            String tn = sr.getTicketNo();
            return tn == null ? "" : tn;
        }).setHeader("Ticket No")
          .setAutoWidth(true)
          .setSortable(true);
        tableGrid.addColumn(sr -> {
            ReservationStatus rs = sr.getReservationStatus();
            return rs != null ? rs.name() : "";
        }).setHeader("Status")
          .setAutoWidth(true)
          .setSortable(true);

        add(badges, form, tableGrid);
    }

    private TextField roField(String label) {
        TextField tf = new TextField(label);
        tf.setReadOnly(true);
        tf.setWidthFull();
        return tf;
    }

    public void setReservation(ReservationDetail reservationDto) {
        this.reservationDetail = reservationDto;

        // Safe guards for nulls
        String ref = reservationDto.getReferenceNo() == null ? "" : reservationDto.getReferenceNo();
        String sellerName = reservationDto.getSellerDetail() == null ? "" : reservationDto.getSellerDetail().getName();
        String tableName = reservationDto.getSeatReservations() == null || reservationDto.getSeatReservations().isEmpty()
                ? ""
                : reservationDto.getSeatReservations().stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(sr -> sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                            ? sr.getSeatDetail().getTableDetail().getTableName()
                            : "")
                    .orElse("");
        int count = reservationDto.getSeatReservations() == null ? 0 : reservationDto.getSeatReservations().size();
        String desc = reservationDto.getDescription() == null ? "" : reservationDto.getDescription();

        referenceNo.setValue(ref);
        seller.setValue(sellerName);
        table.setValue(tableName);
        seats.setValue(String.valueOf(count));
        description.setValue(desc);

        // Ticket breakdown
        int fullCount = reservationDto.getSeatReservations() == null ? 0
                : (int) reservationDto.getSeatReservations().stream()
                    .filter(sr -> sr.getTicketType() == TicketType.FULL)
                    .count();
        int halfCount = reservationDto.getSeatReservations() == null ? 0
                : (int) reservationDto.getSeatReservations().stream()
                    .filter(sr -> sr.getTicketType() == TicketType.HALF)
                    .count();

        fullBadge.setText("FULL: " + fullCount);
        halfBadge.setText("HALF: " + halfCount);

        // Reservation status badge
        ReservationStatus rs = reservationDto.getReservationStatus();
        String statusText = rs == null ? "-" : rs.name();
        statusBadge.setText("Status: " + statusText);
        applyStatusTheme(rs);

        // Grid items
        tableGrid.setItems(reservationDto.getSeatReservations() == null
                ? java.util.List.of()
                : reservationDto.getSeatReservations());
    }

    private void applyStatusTheme(ReservationStatus status) {
        var themes = statusBadge.getElement().getThemeList();
        themes.clear();
        // Always keep "badge"
        themes.add("badge");
        if (status == null) return;

        // Color coding
        switch (status) {
            case PAYMENT_PENDING -> themes.add("warning");
            case PAID -> themes.add("success");
            case CHECKED_IN -> themes.add("contrast");
        }
    }

    public ReservationDetail getReservationDetail() {
        return this.reservationDetail;
    }
}
