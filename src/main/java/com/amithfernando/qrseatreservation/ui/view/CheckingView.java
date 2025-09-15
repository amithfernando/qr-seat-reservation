package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.wontlost.zxing.Constants;
import com.wontlost.zxing.ZXingVaadinReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.amithfernando.qrseatreservation.core.service.ReservationService;
import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;
import com.vaadin.flow.component.grid.Grid;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;

@Route(value = "checkin", layout = MainLayout.class)
@RolesAllowed({"ADMIN","ENTRANCE"})
@PageTitle("Check In")
@Slf4j
public class CheckingView extends PageLayout {

    private final ReservationService reservationService;
    private final TableDetailService tableDetailService;

    // Top stats badges
    private final Span checkedInNo = new Span("-");
    private final Span availableNo = new Span("-");
    private final Span unavailableNo = new Span("-");
    private final Span reservedNo = new Span("-");

    // Search bar (moved into dialog)
    private final TextField searchTicketField = new TextField("Ticket #");
    private final Button searchBtn = new Button("Find");

    // New Check-in dialog
    private final Dialog checkDialog = new Dialog();
    private final Button newCheckBtn = new Button("New checking", VaadinIcon.PLUS_CIRCLE.create());
    private final ZXingVaadinReader zXingVaadin = new ZXingVaadinReader();

    // Dialog elements (seat-only)
    private final Dialog detailsDialog = new Dialog();
    private final Button checkInBtn = new Button("Check-in");
    private final Button closeBtn = new Button("Close");
    private final TextField ticketNoField = roField("Ticket #");
    private final TextField sellerField = roField("Seller");
    private final TextField tableField = roField("Table");
    private final TextField seatNoField = roField("Seat");
    private final TextField typeField = roField("Type");
    private final TextField statusField = roField("Status");

    // Recent check-ins grid + refresh
    private final Grid<SeatReservation> recentGrid = new Grid<>(SeatReservation.class, false);
    private final Button refreshRecentBtn = new Button("Refresh", VaadinIcon.REFRESH.create());

    private String lastScannedTicketNo;

    public CheckingView(ReservationService reservationService, TableDetailService tableDetailService) {
        super(VaadinIcon.QRCODE,"Check In");
        this.reservationService = reservationService;
        this.tableDetailService = tableDetailService;
        setSizeFull();

        // Stats on top
        addStatsRow();
        refreshStats();

        // Seat-only dialog
        buildDetailsDialog();

        // New Check-in dialog (scanner + search)
        buildCheckDialog();

        // Toolbar: open dialog
        newCheckBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newCheckBtn.addClickListener(e -> {
            // Reset scanner and search field for a clean start
            try { zXingVaadin.reset(); } catch (Exception ignored) {}
            searchTicketField.clear();
            checkDialog.open();
        });
        addToContent(new HorizontalLayout(newCheckBtn));

        // Recent check-ins section
        buildRecentGrid();
        loadRecentCheckedIn();
    }

    private void buildDetailsDialog() {
        detailsDialog.setHeaderTitle("Seat details");
        detailsDialog.setModal(true);
        detailsDialog.setDraggable(true);
        detailsDialog.setResizable(true);
        detailsDialog.setWidth("520px");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("480px", 2));
        form.add(ticketNoField, typeField, sellerField, tableField, seatNoField, statusField);
        form.setColspan(ticketNoField, 2);
        form.setColspan(sellerField, 2);
        form.setColspan(tableField, 1);
        form.setColspan(seatNoField, 1);
        form.setColspan(statusField, 2);

        detailsDialog.removeAll();
        detailsDialog.add(form);

        // Footer actions
        checkInBtn.addThemeVariants(ButtonVariant.LUMO_ERROR); // RED button
        checkInBtn.addClickListener(e -> onCheckIn());
        closeBtn.addClickListener(e -> detailsDialog.close());
        HorizontalLayout footer = new HorizontalLayout(closeBtn, checkInBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        detailsDialog.getFooter().removeAll();
        detailsDialog.getFooter().add(footer);

        addToContent(detailsDialog);
    }

    private TextField roField(String label) {
        TextField tf = new TextField(label);
        tf.setReadOnly(true);
        tf.setWidthFull();
        return tf;
    }

    private void populateSeatOnly(SeatReservation sr) {
        ticketNoField.setValue(sr.getTicketNo() == null ? "" : sr.getTicketNo());
        typeField.setValue(sr.getTicketType() == null ? "" : sr.getTicketType().name());
        if (sr.getSeatDetail() != null) {
            seatNoField.setValue(sr.getSeatDetail().getSeatNo() == null ? "" : sr.getSeatDetail().getSeatNo());
            tableField.setValue(
                    sr.getSeatDetail().getTableDetail() == null ? "" :
                            sr.getSeatDetail().getTableDetail().getTableName()
            );
        } else {
            seatNoField.setValue("");
            tableField.setValue("");
        }
        statusField.setValue(sr.getReservationStatus() == null ? "" : sr.getReservationStatus().name());
    }

    private void openDialogForTicket(String rawTicketNo) {
        if (StringUtils.isBlank(rawTicketNo)) {
            notifyError("Invalid ticket number");
            return;
        }
        String ticketNo = rawTicketNo.trim();
        try {
            ReservationDetail rd = reservationService.findReservationByTicketNo(ticketNo);
            if (rd == null || rd.getSeatReservations() == null) {
                notifyError("Ticket not found: " + ticketNo);
                return;
            }
            SeatReservation seatRes = rd.getSeatReservations().stream()
                    .filter(sr -> sr != null && ticketNo.equals(sr.getTicketNo()))
                    .findFirst().orElse(null);
            if (seatRes == null) {
                notifyError("Ticket not found: " + ticketNo);
                return;
            }
            lastScannedTicketNo = ticketNo;

            // Fill dialog fields
            sellerField.setValue(rd.getSellerDetail() != null && rd.getSellerDetail().getName() != null
                    ? rd.getSellerDetail().getName() : "");
            populateSeatOnly(seatRes);

            // Enable check-in only when reservation status is PAID and seat not already checked in
            boolean canCheckIn = rd.getReservationStatus() == ReservationStatus.PAID
                    && seatRes.getReservationStatus() != ReservationStatus.CHECKED_IN;
            checkInBtn.setEnabled(canCheckIn);

            detailsDialog.open();
        } catch (Exception ex) {
            log.error("Failed to load reservation for ticket {}", ticketNo, ex);
            notifyError("Failed to load ticket: " + ex.getMessage());
        }
    }

    private void onCheckIn() {
        if (StringUtils.isBlank(lastScannedTicketNo)) {
            notifyError("No ticket to check-in.");
            return;
        }
        try {
            // Enforce: only if reservation is PAID
            ReservationDetail rd = reservationService.findReservationByTicketNo(lastScannedTicketNo);
            if (rd == null) {
                notifyError("Ticket not found.");
                return;
            }
            if (rd.getReservationStatus() != ReservationStatus.PAID) {
                notifyError("Check-in allowed only when reservation is PAID.");
                return;
            }

            boolean ok = reservationService.checkInByTicketNo(lastScannedTicketNo);
            if (ok) {
                // Refresh only the seat fields after check-in (and keep seller name)
                ReservationDetail updated = reservationService.findReservationByTicketNo(lastScannedTicketNo);
                if (updated != null && updated.getSeatReservations() != null) {
                    sellerField.setValue(updated.getSellerDetail() != null && updated.getSellerDetail().getName() != null
                            ? updated.getSellerDetail().getName() : "");
                    updated.getSeatReservations().stream()
                            .filter(sr -> sr != null && lastScannedTicketNo.equals(sr.getTicketNo()))
                            .findFirst()
                            .ifPresent(this::populateSeatOnly);
                }
                Notification n = Notification.show("Checked-in ticket " + lastScannedTicketNo, 2500, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                checkInBtn.setEnabled(false);

                // Refresh stats + recent grid
                refreshStats();
                loadRecentCheckedIn();
            } else {
                notifyError("Ticket not found or already checked-in.");
            }
        } catch (Exception ex) {
            log.error("Check-in failed for {}", lastScannedTicketNo, ex);
            notifyError("Failed to check-in: " + ex.getMessage());
        }
    }

    private void buildRecentGrid() {
        recentGrid.addColumn(sr -> {
            var ud = getUpdatedAtSafe(sr);
            return ud == null ? "" : ud.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }).setHeader("Updated At").setAutoWidth(true).setSortable(true);

        recentGrid.addColumn(SeatReservation::getTicketNo).setHeader("Ticket #").setAutoWidth(true);
        recentGrid.addColumn(sr -> sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                ? sr.getSeatDetail().getTableDetail().getTableName() : "")
                .setHeader("Table").setAutoWidth(true);
        recentGrid.addColumn(sr -> sr.getSeatDetail() != null ? sr.getSeatDetail().getSeatNo() : "")
                .setHeader("Seat").setAutoWidth(true);
        recentGrid.addColumn(sr -> sr.getTicketType() != null ? sr.getTicketType().name() : "")
                .setHeader("Type").setAutoWidth(true);
        recentGrid.addColumn(sr -> sr.getReservationStatus() != null ? sr.getReservationStatus().name() : "")
                .setHeader("Status").setAutoWidth(true);

        recentGrid.setWidthFull();
        recentGrid.setHeight("40vh");
        recentGrid.getElement().getThemeList().add("row-stripes");

        refreshRecentBtn.addClickListener(e -> loadRecentCheckedIn());
        refreshRecentBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout header = new HorizontalLayout(new H3("Recent check-ins"), refreshRecentBtn);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);

        addToContent(header, recentGrid);
    }

    private void loadRecentCheckedIn() {
        try {
            List<SeatReservation> recent = reservationService.getAllReservations().stream()
                    .filter(r -> r.getSeatReservations() != null)
                    .flatMap(r -> r.getSeatReservations().stream())
                    .filter(sr -> sr != null && sr.getReservationStatus() == ReservationStatus.CHECKED_IN)
                    .sorted(Comparator.comparing(this::getUpdatedAtSafe, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(50)
                    .collect(Collectors.toList());
            recentGrid.setItems(recent);
        } catch (Exception ex) {
            log.error("Failed to load recent check-ins", ex);
            notifyError("Failed to load recent check-ins: " + ex.getMessage());
        }
    }

    // New: dialog containing scanner and search
    private void buildCheckDialog() {
        checkDialog.setHeaderTitle("New check-in");
        checkDialog.setModal(true);
        checkDialog.setDraggable(true);
        checkDialog.setResizable(true);
        checkDialog.setWidth("720px");

        // Search bar inside dialog
        searchTicketField.setPlaceholder("Enter ticket no");
        searchTicketField.setClearButtonVisible(true);
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> {
            String ticketNo = searchTicketField.getValue();
            openDialogForTicket(ticketNo);
            // Keep dialog open for subsequent scans/searches
        });
        HorizontalLayout searchRow = new HorizontalLayout(searchTicketField, searchBtn);
        searchRow.setAlignItems(Alignment.END);
        searchRow.setWidthFull();

        // Scanner inside dialog
        zXingVaadin.setFrom(Constants.From.camera);
        zXingVaadin.setId("video"); // id must be 'video' if From.camera
        zXingVaadin.setWidth("350");
        zXingVaadin.setStyle("border : 1px solid gray");

        zXingVaadin.onZxingErrorListener = e -> {
            String msg = "Unknown error accessing the camera";
            if ("AbortError".equals(e.name)) {
                msg = "Unspecified error preventing the use of the camera";
            } else if ("NotAllowedError".equals(e.name)) {
                msg = "Browser denies access to the camera. Please allow access to the camera in your browser and/or use secure connection";
            } else if ("NotFoundError".equals(e.name) || "OverconstrainedError".equals(e.name)) {
                msg = "No camera found";
            } else if ("NotReadableError".equals(e.name)) {
                msg = "Hardware error";
            }
            if (StringUtils.isNotBlank(e.name)) {
                msg = msg + " (" + e.name + ")";
            }
            log.warn("Camera error: {}", msg);
        };

        zXingVaadin.addValueChangeListener(e -> {
            String ticketNo = e.getValue();
            log.info("QR scanned: {}", ticketNo);
            openDialogForTicket(ticketNo);
            // Allow scanning the next code while dialog is open
            zXingVaadin.reset();
        });

        VerticalLayout content = new VerticalLayout(searchRow, zXingVaadin);
        content.setPadding(false);
        content.setSpacing(true);
        checkDialog.removeAll();
        checkDialog.add(content);

        Button close = new Button("Close", ev -> checkDialog.close());
        checkDialog.getFooter().removeAll();
        checkDialog.getFooter().add(new HorizontalLayout(close));

        addToContent(checkDialog);
    }

    // Safely obtain updatedAt if available on entity; otherwise return null
    private java.time.LocalDateTime getUpdatedAtSafe(SeatReservation sr) {
        try {
            return (java.time.LocalDateTime) SeatReservation.class
                    .getMethod("getUpdatedAt")
                    .invoke(sr);
        } catch (Exception ignored) {
            return null;
        }
    }

    // Build the top stats row
    private void addStatsRow() {
        Div checkedInCard = statCard(VaadinIcon.CHECK_SQUARE, "Total check-ins", checkedInNo, "var(--lumo-success-color)");
        Div availableCard = statCard(VaadinIcon.CHECK_CIRCLE, "Available seats", availableNo, "var(--lumo-primary-color)");
        Div unavailableCard = statCard(VaadinIcon.CLOSE_CIRCLE, "Unavailable seats", unavailableNo, "var(--lumo-error-color)");
        Div reservedCardDiv = statCard(VaadinIcon.BOOKMARK, "Reserved seats", reservedNo, "var(--lumo-contrast)");

        HorizontalLayout statsRow = new HorizontalLayout(checkedInCard, availableCard, unavailableCard, reservedCardDiv);
        statsRow.setWidthFull();
        statsRow.setSpacing(true);
        statsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        addToContent(statsRow);
    }

    private Div statCard(VaadinIcon icon, String label, Span valueSpan, String color) {
        Icon i = icon.create();
        i.setSize("26px");
        i.getStyle().set("color", color);

        Span title = new Span(label);
        title.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        valueSpan.getStyle().set("font-size", "22px").set("font-weight", "700");

        Div content = new Div(title, valueSpan);
        content.getStyle().set("display", "flex").set("flex-direction", "column");

        HorizontalLayout row = new HorizontalLayout(i, content);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.setPadding(false);

        Div card = new Div(row);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "200px")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)");
        return card;
    }

    private void refreshStats() {
        try {
            // Total check-ins = seats with CHECKED_IN
            long totalCheckedIn = reservationService.getAllReservations().stream()
                    .filter(r -> r.getSeatReservations() != null)
                    .flatMap(r -> r.getSeatReservations().stream())
                    .filter(sr -> sr != null && sr.getReservationStatus() == ReservationStatus.CHECKED_IN)
                    .count();

            // Seat availability across all tables
            var allTables = tableDetailService.getAllTables();
            long available = allTables.stream().mapToLong(t -> t.getAvailableSeats()).sum();
            long unavailable = allTables.stream().mapToLong(t -> t.getUnavailableSeats()).sum();
            long reserved = allTables.stream().mapToLong(t -> t.getReservedSeats()).sum();

            checkedInNo.setText(String.valueOf(totalCheckedIn));
            availableNo.setText(String.valueOf(available));
            unavailableNo.setText(String.valueOf(unavailable));
            reservedNo.setText(String.valueOf(reserved));
        } catch (Exception ex) {
            log.warn("Failed to refresh stats: {}", ex.getMessage());
        }
    }

    private void notifyError(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}