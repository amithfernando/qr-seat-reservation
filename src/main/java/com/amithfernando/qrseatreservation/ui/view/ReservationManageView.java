package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.amithfernando.qrseatreservation.core.service.ReservationService;
import com.amithfernando.qrseatreservation.core.service.SellerDetailService;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import com.amithfernando.qrseatreservation.core.enums.TicketType;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import com.amithfernando.qrseatreservation.core.enums.ReservationStatus;
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
    // Form fields (used inside dialog)
    private final Select<TableDetail> tableSelect = new Select<>();
    private final MultiSelectComboBox<SeatDetail> seatSelect = new MultiSelectComboBox<>();
    private final TextField noOfSelectedSeats = new TextField("No. of selected seats");
    private final Select<SellerDetail> sellerSelect = new Select<>();
    private final TextField description = new TextField("Description");
    // Add inputs for FULL and HALF ticket counts
    private final IntegerField fullCountField = new IntegerField("No. of FULL tickets");
    private final IntegerField halfCountField = new IntegerField("No. of HALF tickets");
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");

    // Grid
    private final Grid<ReservationDetail> grid = new Grid<>(ReservationDetail.class, false);

    // View dialog for row details
    private final Dialog viewDialog = new Dialog();
    private final com.amithfernando.qrseatreservation.ui.view.component.ReservationViewDialogComponent reservationViewDialogComponent =
            new com.amithfernando.qrseatreservation.ui.view.component.ReservationViewDialogComponent();
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

        // Table
        tableSelect.setLabel("Table");
        tableSelect.setItemLabelGenerator(TableDetail::getTableNoWithStatus);
        tableSelect.setRequiredIndicatorVisible(true);
        tableSelect.setHelperText("Choose a table to see its available seats.");
        tableSelect.addValueChangeListener(e -> {
            TableDetail table = e.getValue();
            if (table == null) {
                seatSelect.clear();
                seatSelect.setItems(List.of());
                noOfSelectedSeats.clear();
                return;
            }
            TableDetail fresh = tableDetailService.getAllTables().stream()
                    .filter(t -> t.getId().equals(table.getId()))
                    .findFirst()
                    .orElse(table);
            List<SeatDetail> available = fresh.getSeatDetails().stream()
                    .filter(SeatDetail::isAvailable)
                    .collect(Collectors.toList());
            seatSelect.setItems(available);
            seatSelect.clear();
        });

        // Seats
        seatSelect.setLabel("Seats");
        seatSelect.setItemLabelGenerator(SeatDetail::getNoWithStatus);
        seatSelect.setClearButtonVisible(true);
        seatSelect.setPlaceholder("Select one or more seats");
        seatSelect.addSelectionListener(e ->{
            noOfSelectedSeats.setValue(String.valueOf(seatSelect.getSelectedItems().size()));
        });

        noOfSelectedSeats.setEnabled(false);

        // Seller
        sellerSelect.setLabel("Seller");
        sellerSelect.setItemLabelGenerator(SellerDetail::getName);
        sellerSelect.setRequiredIndicatorVisible(true);

        // Configure ticket count fields
        fullCountField.setMin(0);
        halfCountField.setMin(0);
        fullCountField.setStepButtonsVisible(true);
        halfCountField.setStepButtonsVisible(true);
        fullCountField.setClearButtonVisible(true);
        halfCountField.setClearButtonVisible(true);
        fullCountField.setHelperText("Number of FULL tickets");
        halfCountField.setHelperText("Number of HALF tickets");

        // Description
        description.setClearButtonVisible(true);
        description.setPlaceholder("Optional");

        // Actions
        saveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickListener(e -> onSave());
        cancelBtn.addClickListener(e -> reservationDialog.close());

        // Organized, responsive form layout
        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Row 1: Table | Seller
        form.add(tableSelect, sellerSelect);

        // Row 2: Seats (spans 2)
        form.add(seatSelect);
        form.setColspan(seatSelect, 2);

        //Row 3: no of selected seats (spans 3)
        form.add(noOfSelectedSeats);
        form.setColspan(noOfSelectedSeats, 2);

        // Row 4: FULL | HALF
        form.add(fullCountField, halfCountField);

        // Row 5: Description (spans 2)
        form.add(description);
        form.setColspan(description, 2);

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

        TableDetail table = tableSelect.getValue();
        Set<SeatDetail> seats = new HashSet<>(seatSelect.getSelectedItems());
        SellerDetail seller = sellerSelect.getValue();
        String desc = description.getValue();

        // Read counts (default to 0 if empty)
        int fullCount = fullCountField.getValue() == null ? 0 : fullCountField.getValue();
        int halfCount = halfCountField.getValue() == null ? 0 : halfCountField.getValue();

        // Validate counts vs selected seats
        int totalSelected = seats.size();
        if (fullCount < 0 || halfCount < 0) {
            showError("Ticket counts cannot be negative");
            return;
        }
        if (fullCount + halfCount != totalSelected) {
            showError("FULL + HALF must equal the number of selected seats (" + totalSelected + ")");
            return;
        }

        try {
            // Assign types: first FULL, then HALF
            var it = seats.iterator();
            Set<SeatReservation> seatReservations = new HashSet<>();
            for (int i = 0; i < fullCount && it.hasNext(); i++) {
                SeatDetail sd = it.next();
                seatReservations.add(SeatReservation.builder()
                        .seatDetail(sd)
                        .ticketType(TicketType.FULL)
                        .build());
            }
            for (int i = 0; i < halfCount && it.hasNext(); i++) {
                SeatDetail sd = it.next();
                seatReservations.add(SeatReservation.builder()
                        .seatDetail(sd)
                        .ticketType(TicketType.HALF)
                        .build());
            }

            ReservationDetail reservation = ReservationDetail.builder()
                    .sellerDetail(seller)
                    .seatReservations(seatReservations)
                    .description(desc)
                    .build();

            reservationService.saveReservation(reservation);
            Notification.show("Reservation created", 2500, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            reservationDialog.close();
            clearForm();
            refreshGrid();

        } catch (Exception ex) {
            Notification n = Notification.show("Failed to create reservation: " + ex.getMessage(),
                    3500, Notification.Position.MIDDLE);
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
        if (seatSelect.getSelectedItems() == null || seatSelect.getSelectedItems().isEmpty()) {
            showError("Please select at least one seat");
            return false;
        }
        return true;
    }

    private void clearForm() {
        description.clear();
        seatSelect.clear();
        fullCountField.clear();
        halfCountField.clear();
        // keep table and seller selected for convenience if dialog re-opened
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
        viewDialog.setWidth("600px");

        viewDialog.removeAll();
        viewDialog.add(reservationViewDialogComponent);

        Button closeBtn = new Button("Close", e -> viewDialog.close());
        Button deleteBtn = new Button("Delete", e -> confirmAndDelete());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        markPaidBtn = new Button("Mark as PAID", e -> confirmAndMarkPaid());
        markPaidBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        // add: download link + button
        downloadBtn = new Button("Download Tickets");
        downloadLink = new Anchor();
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(downloadBtn);

        HorizontalLayout footer = new HorizontalLayout(closeBtn, downloadLink, markPaidBtn, deleteBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        viewDialog.getFooter().removeAll();
        viewDialog.getFooter().add(footer);

        add(viewDialog);
    }

    private void openReservationDetails(ReservationDetail reservationDetail) {
        reservationViewDialogComponent.setReservation(reservationDetail);
        // Enable "Mark as PAID" only if currently not PAID
        boolean canMarkPaid = reservationDetail.getReservationStatus() != com.amithfernando.qrseatreservation.core.enums.ReservationStatus.PAID;
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
        confirm.setHeaderTitle("Mark reservation as PAID?");
        confirm.add("Confirm payment for reservation " +
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