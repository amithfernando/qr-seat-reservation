package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.service.ReservationService;
import com.qrseatreservation.core.service.SellerDetailService;
import com.qrseatreservation.core.service.TableDetailService;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.qrseatreservation.ui.view.component.TicketPreviewDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import com.qrseatreservation.core.enums.TicketType;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import com.qrseatreservation.core.enums.ReservationStatus;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "reservations", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Create Reservation")
public class ReservationManageView extends PageLayout {

    private final ReservationService reservationService;
    private final SellerDetailService sellerDetailService;
    private final TableDetailService tableDetailService;

    // Dialog + trigger
    private final Dialog reservationDialog = new Dialog();
    private final Button newReservationBtn = new Button("New reservation");
    // Toolbar search
    private final TextField searchField = new TextField();
    // Form fields
    private final Select<TableDetail> tableSelect = new Select<>();
    private final Select<SellerDetail> sellerSelect = new Select<>();
    private final TextField description = new TextField("Description");
    // Ticket counts — system auto-assigns seats
    private final IntegerField fullCountField = new IntegerField("FULL tickets");
    private final IntegerField halfCountField = new IntegerField("HALF tickets");
    private final IntegerField invitationCountField = new IntegerField("INVITATION tickets (complimentary)");
    private final Span availableSeatsLabel = new Span();
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");

    // Grid
    private final Grid<ReservationDetail> grid = new Grid<>(ReservationDetail.class, false);

    // View dialog for row details
    private final Dialog viewDialog = new Dialog();
    private final com.qrseatreservation.ui.view.component.ReservationViewDialogComponent reservationViewDialogComponent =
            new com.qrseatreservation.ui.view.component.ReservationViewDialogComponent();
    private Button markPaidBtn; // add: button to mark reservation as PAID
    private Anchor downloadLink; // add: download anchor
    private Button downloadBtn;  // add: visible button inside the anchor

    public ReservationManageView(ReservationService reservationService,
                                 SellerDetailService sellerDetailService,
                                 TableDetailService tableDetailService) {
        super(VaadinIcon.CALENDAR,"Create Reservation");
        this.reservationService = reservationService;
        this.sellerDetailService = sellerDetailService;
        this.tableDetailService = tableDetailService;

        setSizeFull();
        buildToolbar();
        buildDialog();
        buildGrid();
        buildViewDialog();
        loadData();
    }

    private void buildToolbar() {
        newReservationBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newReservationBtn.addClickListener(e -> {
            clearForm();
            // Ensure choices are fresh before opening
            tableSelect.setItems(tableDetailService.getAllTables());
            sellerSelect.setItems(sellerDetailService.getAllSellers());
            reservationDialog.open();
        });

        // Search box
        searchField.setPlaceholder("Search reservations...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.addValueChangeListener(e -> applyFilter(e.getValue()));

        HorizontalLayout toolbar = new HorizontalLayout(newReservationBtn, searchField);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.getStyle().set("padding-bottom", "var(--lumo-space-s)");

        addToContent(toolbar);
    }

    private void buildDialog() {
        reservationDialog.setHeaderTitle("Create reservation");
        reservationDialog.setModal(true);
        reservationDialog.setDraggable(true);
        reservationDialog.setResizable(true);
        reservationDialog.setWidth("720px");

        // Table — on change, show available seat count
        tableSelect.setLabel("Table");
        tableSelect.setItemLabelGenerator(TableDetail::getTableNoWithStatus);
        tableSelect.setRequiredIndicatorVisible(true);
        tableSelect.addValueChangeListener(e -> {
            TableDetail table = e.getValue();
            if (table == null) {
                availableSeatsLabel.setText("");
                return;
            }
            TableDetail fresh = tableDetailService.getAllTables().stream()
                    .filter(t -> t.getId().equals(table.getId()))
                    .findFirst().orElse(table);
            long avail = fresh.getSeatDetails().stream().filter(SeatDetail::isAvailable).count();
            availableSeatsLabel.setText(avail + " seats available in this table");
            availableSeatsLabel.getStyle().set("color", avail > 0 ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
        });

        // Seller
        sellerSelect.setLabel("Seller");
        sellerSelect.setItemLabelGenerator(SellerDetail::getName);
        sellerSelect.setRequiredIndicatorVisible(true);

        // Ticket count fields
        fullCountField.setMin(0);
        fullCountField.setValue(0);
        fullCountField.setStepButtonsVisible(true);
        fullCountField.setHelperText("Paid €25 tickets");

        halfCountField.setMin(0);
        halfCountField.setValue(0);
        halfCountField.setStepButtonsVisible(true);
        halfCountField.setHelperText("Half-price tickets");

        invitationCountField.setMin(0);
        invitationCountField.setValue(0);
        invitationCountField.setStepButtonsVisible(true);
        invitationCountField.setHelperText("Complimentary — auto-marked PAID, excluded from revenue");

        // Description
        description.setClearButtonVisible(true);
        description.setPlaceholder("Optional");

        // Actions
        saveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickListener(e -> onSave());
        cancelBtn.addClickListener(e -> reservationDialog.close());

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );

        // Row 1: Table (2 cols) | Seller (1 col)
        form.add(tableSelect, sellerSelect);
        form.setColspan(tableSelect, 2);

        // Available label (full width)
        availableSeatsLabel.getStyle().set("font-size", "13px").set("margin-bottom", "4px");
        form.add(availableSeatsLabel);
        form.setColspan(availableSeatsLabel, 3);

        // Row: FULL | HALF | INVITATION
        form.add(fullCountField, halfCountField, invitationCountField);

        // Row: Description (full width)
        form.add(description);
        form.setColspan(description, 3);

        reservationDialog.removeAll();
        reservationDialog.add(form);

        // Footer aligned actions
        HorizontalLayout footer = new HorizontalLayout(cancelBtn, saveBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        reservationDialog.getFooter().removeAll();
        reservationDialog.getFooter().add(footer);

        addToContent(reservationDialog);
    }

    private void buildGrid() {
        grid.addColumn(ReservationDetail::getReferenceNo)
                .setHeader("Reference #")
                .setAutoWidth(true)
                .setSortable(true);

        // Status with badge look
        grid.addColumn(new ComponentRenderer<>(rd -> {
            Span badge = new Span(String.valueOf(rd.getReservationStatus()));
            badge.getElement().getThemeList().add("badge");
            if (rd.getReservationStatus() == ReservationStatus.PAID) {
                badge.getElement().getThemeList().add("success");
            } else {
                badge.getElement().getThemeList().add("contrast");
            }
            return badge;
        })).setHeader("Status").setAutoWidth(true).setSortable(true);

        grid.addColumn(r -> r.getSellerDetail() == null ? "" : r.getSellerDetail().getName())
                .setHeader("Seller")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ReservationDetail::getDescription)
                .setHeader("Description")
                .setSortable(true)
                .setFlexGrow(2);

        grid.addColumn(r -> {
            if (r.getSeatReservations() == null || r.getSeatReservations().isEmpty()) return "";
            return r.getSeatReservations().stream()
                    .findFirst()
                    .map(sr -> sr.getSeatDetail().getTableDetail().getTableName())
                    .orElse("");
        }).setHeader("Table")
                .setAutoWidth(true);

        grid.addColumn(r ->
                r.getSeatReservations() == null ? "0" : String.valueOf(r.getSeatReservations().size()))
                .setHeader("No. of Seats")
                .setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("60vh");

        // Visual improvements
        grid.getElement().getThemeList().add("row-stripes");
        grid.getElement().getThemeList().add("wrap-cell-content");
        grid.setColumnReorderingAllowed(true);

        // Open details dialog on row selection
        grid.addSelectionListener(e -> {
            e.getFirstSelectedItem().ifPresent(this::openReservationDetails);
            grid.deselectAll();
        });

        addToContent(grid);
    }

    private void loadData() {
        // Preload for first dialog open; also refreshed on open
        sellerSelect.setItems(sellerDetailService.getAllSellers());
        tableSelect.setItems(tableDetailService.getAllTables());
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(reservationService.getAllReservations());
        // Optionally re-apply current filter
        applyFilter(searchField.getValue());
    }

    private void applyFilter(String filterText) {
        String ft = filterText == null ? "" : filterText.trim().toLowerCase();
        grid.getListDataView().removeFilters();
        if (!ft.isEmpty()) {
            grid.getListDataView().addFilter(r -> {
                String ref = r.getReferenceNo() == null ? "" : r.getReferenceNo().toLowerCase();
                String seller = r.getSellerDetail() == null || r.getSellerDetail().getName() == null ? "" : r.getSellerDetail().getName().toLowerCase();
                String desc = r.getDescription() == null ? "" : r.getDescription().toLowerCase();
                String status = r.getReservationStatus() == null ? "" : r.getReservationStatus().name().toLowerCase();
                return ref.contains(ft) || seller.contains(ft) || desc.contains(ft) || status.contains(ft);
            });
        }
    }

    private void onSave() {
        if (!validateForm()) return;

        TableDetail tableValue = tableSelect.getValue();
        SellerDetail seller = sellerSelect.getValue();
        String desc = description.getValue();

        int fullCount = fullCountField.getValue() == null ? 0 : fullCountField.getValue();
        int halfCount = halfCountField.getValue() == null ? 0 : halfCountField.getValue();
        int invitationCount = invitationCountField.getValue() == null ? 0 : invitationCountField.getValue();
        int totalNeeded = fullCount + halfCount + invitationCount;

        if (totalNeeded == 0) {
            showError("Enter at least one ticket count");
            return;
        }

        // Get fresh list of available seats, sorted
        TableDetail fresh = tableDetailService.getAllTables().stream()
                .filter(t -> t.getId().equals(tableValue.getId()))
                .findFirst().orElse(tableValue);
        List<SeatDetail> available = fresh.getSeatDetails().stream()
                .filter(SeatDetail::isAvailable)
                .sorted(Comparator.comparing(SeatDetail::getSeatNo))
                .collect(Collectors.toList());

        if (totalNeeded > available.size()) {
            showError("Not enough available seats. Need " + totalNeeded + ", only " + available.size() + " available.");
            return;
        }

        try {
            int idx = 0;
            Set<SeatReservation> seatReservations = new HashSet<>();
            for (int i = 0; i < fullCount; i++) {
                seatReservations.add(SeatReservation.builder()
                        .seatDetail(available.get(idx++))
                        .ticketType(TicketType.FULL)
                        .build());
            }
            for (int i = 0; i < halfCount; i++) {
                seatReservations.add(SeatReservation.builder()
                        .seatDetail(available.get(idx++))
                        .ticketType(TicketType.HALF)
                        .build());
            }
            for (int i = 0; i < invitationCount; i++) {
                seatReservations.add(SeatReservation.builder()
                        .seatDetail(available.get(idx++))
                        .ticketType(TicketType.INVITATION)
                        .build());
            }

            ReservationDetail reservation = ReservationDetail.builder()
                    .sellerDetail(seller)
                    .seatReservations(seatReservations)
                    .description(desc)
                    .build();

            reservationService.saveReservation(reservation);
            Notification.show("Reservation created: " + totalNeeded + " seats auto-assigned",
                    2500, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            reservationDialog.close();
            clearForm();
            refreshGrid();

        } catch (Exception ex) {
            Notification n = Notification.show("Failed: " + ex.getMessage(), 3500, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        if (tableSelect.isEmpty()) {
            showError("Please select a table");
            return false;
        }
        if (sellerSelect.isEmpty()) {
            showError("Please select a seller");
            return false;
        }
        return true;
    }

    private void clearForm() {
        description.clear();
        fullCountField.setValue(0);
        halfCountField.setValue(0);
        invitationCountField.setValue(0);
        availableSeatsLabel.setText("");
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void buildViewDialog() {
        viewDialog.setHeaderTitle("Reservation details");
        viewDialog.setModal(true);
        viewDialog.setDraggable(true);
        viewDialog.setResizable(true);
        viewDialog.setWidth("min(820px, 96vw)");

        viewDialog.removeAll();
        viewDialog.add(reservationViewDialogComponent);

        Button closeBtn = new Button("Close", e -> viewDialog.close());
        Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create(), e -> confirmAndDelete());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        markPaidBtn = new Button("Mark ALL as PAID", e -> confirmAndMarkPaid());
        markPaidBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        // add: download link + button
        downloadBtn = new Button("Download Tickets");
        downloadLink = new Anchor();
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(downloadBtn);

        Button previewBtn = new Button("View Tickets", VaadinIcon.EYE.create(), e -> previewTickets());
        reservationViewDialogComponent.setOnTicketPaidToggle(this::toggleTicketPaid);
        reservationViewDialogComponent.setOnTicketView(this::previewSingleTicket);

        // Wraps on narrow screens so every action (incl. Delete) stays visible
        HorizontalLayout left = new HorizontalLayout(closeBtn, deleteBtn);
        HorizontalLayout right = new HorizontalLayout(previewBtn, downloadLink, markPaidBtn);
        left.getStyle().set("flex-wrap", "wrap");
        right.getStyle().set("flex-wrap", "wrap").set("margin-left", "auto");
        right.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        HorizontalLayout footer = new HorizontalLayout(left, right);
        footer.setWidthFull();
        footer.getStyle().set("flex-wrap", "wrap").set("row-gap", "var(--lumo-space-s)");
        viewDialog.getFooter().removeAll();
        viewDialog.getFooter().add(footer);

        add(viewDialog);
    }

    private void openReservationDetails(ReservationDetail reservationDetail) {
        reservationViewDialogComponent.setReservation(reservationDetail);
        // Enable "Mark as PAID" only if currently not PAID
        boolean canMarkPaid = reservationDetail.getReservationStatus() != com.qrseatreservation.core.enums.ReservationStatus.PAID;
        if (markPaidBtn != null) {
            markPaidBtn.setEnabled(canMarkPaid);
            markPaidBtn.setVisible(true);
        }
        // update download resource for the selected reservation
        updateDownloadResource();
        viewDialog.open();
    }

    // add: prepare stream resource for tickets zip using ReservationService (which uses TicketService)
    private void updateDownloadResource() {
        ReservationDetail current = reservationViewDialogComponent.getReservationDetail();
        if (current == null) {
            downloadLink.setHref("");
            downloadBtn.setEnabled(false);
            return;
        }
        try {
            byte[] zipBytes = reservationService.getTicketImageZip(current);
            if (zipBytes == null || zipBytes.length == 0) {
                downloadLink.setHref("");
                downloadBtn.setEnabled(false);
                return;
            }
            StreamResource resource = new StreamResource(
                    current.getTicketFileName() + ".zip",
                    () -> new java.io.ByteArrayInputStream(zipBytes)
            );
            downloadLink.setHref(resource);
            downloadBtn.setEnabled(true);
        } catch (Exception ex) {
            downloadLink.setHref("");
            downloadBtn.setEnabled(false);
            Notification n = Notification.show("Failed to prepare tickets: " + ex.getMessage(),
                    3500, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void toggleTicketPaid(SeatReservation sr, boolean paid) {
        try {
            ReservationDetail updated = reservationService.setTicketPaid(sr.getId(), paid);
            reservationViewDialogComponent.setReservation(updated);
            if (markPaidBtn != null) markPaidBtn.setEnabled(updated.getReservationStatus() != ReservationStatus.PAID);
            refreshGrid();
            Notification n = Notification.show("Ticket " + sr.getTicketNo() + (paid ? " marked PAID" : " marked unpaid"),
                    2000, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            showError("Failed to update ticket: " + ex.getMessage());
        }
    }

    private void previewSingleTicket(SeatReservation sr) {
        try {
            TicketPreviewDialog.open("Ticket " + sr.getTicketNo(),
                    List.of(new TicketPreviewDialog.Item(sr.getTicketNo(), reservationService.renderTicketImage(sr))));
        } catch (Exception ex) {
            showError("Failed to render ticket: " + ex.getMessage());
        }
    }

    private void previewTickets() {
        ReservationDetail current = reservationViewDialogComponent.getReservationDetail();
        if (current == null) { showError("No reservation selected."); return; }
        try {
            List<TicketPreviewDialog.Item> items = reservationService.renderTicketImages(current).stream()
                    .map(t -> new TicketPreviewDialog.Item(t.getTicketNo(), t.getData()))
                    .toList();
            TicketPreviewDialog.open("Tickets" + (current.getReferenceNo() != null ? " — #" + current.getReferenceNo() : ""), items);
        } catch (Exception ex) {
            showError("Failed to render tickets: " + ex.getMessage());
        }
    }

    private void confirmAndDelete() {
        ReservationDetail current = reservationViewDialogComponent.getReservationDetail();
        if (current == null) {
            showError("No reservation selected.");
            return;
        }

        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Delete reservation?");
        confirm.add("Are you sure you want to delete reservation " +
                (current.getReferenceNo() != null ? ("#" + current.getReferenceNo()) : "") + "?");

        Button cancel = new Button("Cancel", e -> confirm.close());
        Button confirmDelete = new Button("Delete", e -> {
            try {
                reservationService.deleteReservation(current);
                confirm.close();
                viewDialog.close();
                refreshGrid();
                Notification n = Notification.show("Reservation deleted", 2500, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                confirm.close();
                Notification n = Notification.show("Failed to delete: " + ex.getMessage(),
                        3500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(cancel, confirmDelete);
        actions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        actions.setWidthFull();
        confirm.getFooter().add(actions);

        confirm.setModal(true);
        confirm.open();
    }

    // add: confirmation + action to mark reservation PAID
    private void confirmAndMarkPaid() {
        ReservationDetail current = reservationViewDialogComponent.getReservationDetail();
        if (current == null) {
            showError("No reservation selected.");
            return;
        }
        if (current.getReservationStatus() == ReservationStatus.PAID) {
            Notification n = Notification.show("Reservation is already PAID", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            return;
        }

        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Mark all tickets as PAID?");
        confirm.add("Confirm payment for every ticket in reservation " +
                (current.getReferenceNo() != null ? ("#" + current.getReferenceNo()) : "") + "?");

        Button cancel = new Button("Cancel", e -> confirm.close());
        Button confirmPaid = new Button("Mark as PAID", e -> {
            try {
                reservationService.setPaymentDone(current);
                confirm.close();
                viewDialog.close();
                refreshGrid();
                Notification n = Notification.show("Reservation marked as PAID", 2500, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                confirm.close();
                Notification n = Notification.show("Failed to mark as PAID: " + ex.getMessage(),
                        3500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmPaid.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout actions = new HorizontalLayout(cancel, confirmPaid);
        actions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        actions.setWidthFull();
        confirm.getFooter().add(actions);

        confirm.setModal(true);
        confirm.open();
    }
}