package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;

@Route(value="tableManage", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Table Settings")
public class TableManageView extends PageLayout {

    // Grid state
    private final Grid<TableDetail> grid = new Grid<>(TableDetail.class, false);
    private final List<TableDetail> rows = new ArrayList<>();
    private final ListDataProvider<TableDetail> dataProvider = new ListDataProvider<>(rows);

    // Toolbar
    private final TextField searchField = new TextField();

    // Dialog and form fields
    private final Dialog dialog = new Dialog();
    private final TextField tableName = new TextField("Table name");
    private final TextField tableDescription = new TextField("Table description");
    private final IntegerField totalSeats = new IntegerField("Total number of seats");
    private final IntegerField availableSeats = new IntegerField("Number of available seats");
    private final IntegerField unavailableSeats = new IntegerField("Number of unavailable seats");
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    // Open dialog button
    private final Button openDialogButton = new Button("Add table");


    private final TableDetailService tableDetailService;

    public TableManageView(TableDetailService tableDetailService) {
        super(VaadinIcon.TABLE,"Table Settings");
        this.tableDetailService = tableDetailService;
        setSizeFull();
        initGrid();
        initDialog();
        initActions();
        setContents();

    }

    private void setContents() {
        // Toolbar with primary action and search
        searchField.setPlaceholder("Search tables...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.addValueChangeListener(e -> applyFilter(e.getValue()));

        openDialogButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(openDialogButton, searchField);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setAlignItems(Alignment.END);
        toolbar.getStyle()
                .set("padding-bottom", "var(--lumo-space-s)");

        grid.setWidthFull();
        grid.setHeight("60vh");

        addToContent(toolbar, grid, dialog);
    }

    private void initGrid() {
        grid.addColumn(TableDetail::getTableName).setHeader("Table name").setAutoWidth(true).setSortable(true).setFlexGrow(1);
        grid.addColumn(TableDetail::getDescription).setHeader("Table description").setSortable(true).setFlexGrow(2);
        grid.addColumn(TableDetail::getNoOfSeats).setHeader("Total seats").setAutoWidth(true).setSortable(true);
        grid.addColumn(TableDetail::getAvailableSeats).setHeader("Available").setAutoWidth(true).setSortable(true);
        grid.addColumn(TableDetail::getUnavailableSeats).setHeader("Unavailable").setAutoWidth(true).setSortable(true);
        grid.addColumn(TableDetail::getReservedSeats).setHeader("Reserved").setAutoWidth(true).setSortable(true);
        grid.setItems(dataProvider);

        // Visual and interaction improvements
        grid.getElement().getThemeList().add("row-stripes");
        grid.getElement().getThemeList().add("wrap-cell-content");
        grid.setColumnReorderingAllowed(true);

        rows.addAll(tableDetailService.getAllTables());

        // Context menu with Delete action
        GridContextMenu<TableDetail> contextMenu = grid.addContextMenu();
        GridMenuItem<TableDetail> deleteItem = contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::confirmAndDelete);
        });
        // Optional: disable when no row under cursor
        contextMenu.addGridContextMenuOpenedListener(e -> deleteItem.setEnabled(e.getItem() != null));

    }

    private void initDialog() {
        dialog.setHeaderTitle("Add table");
        // Field configuration
        tableName.setRequiredIndicatorVisible(true);

        //totalSeats.setHasControls(true);
        totalSeats.setMin(1);
        totalSeats.setMax(12);
        totalSeats.setStepButtonsVisible(true);
        totalSeats.setRequiredIndicatorVisible(true);
        totalSeats.setHelperText("How many seats does this table have in total?");

        availableSeats.setMin(0);
        availableSeats.setStepButtonsVisible(true);
        availableSeats.setRequiredIndicatorVisible(true);
        availableSeats.setHelperText("Seats that can be reserved.");

        unavailableSeats.setMin(0);
        unavailableSeats.setStepButtonsVisible(true);
        unavailableSeats.setRequiredIndicatorVisible(true);
        unavailableSeats.setHelperText("Seats blocked or unusable.");

        FormLayout form = new FormLayout(tableName,tableDescription, totalSeats, availableSeats, unavailableSeats);
        dialog.add(form);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new HorizontalLayout(cancelButton, saveButton));

        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setWidth("480px");
    }

    private void initActions() {
        openDialogButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        openDialogButton.addClickListener(e -> {
            clearForm();
            dialog.open();
        });

        cancelButton.addClickListener(e -> dialog.close());

        saveButton.addClickListener(e -> {
            if (!validateForm()) {
                return;
            }
            //save table
            tableDetailService.createTable( tableName.getValue().trim(),availableSeats.getValue(), unavailableSeats.getValue(),tableDescription.getValue());
            gridRefresh();
            dialog.close();
            Notification.show("Row added", 2500, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private void applyFilter(String filterText) {
        String ft = filterText == null ? "" : filterText.trim().toLowerCase();
        dataProvider.clearFilters();
        if (!ft.isEmpty()) {
            dataProvider.addFilter(td -> {
                String name = td.getTableName() == null ? "" : td.getTableName().toLowerCase();
                String desc = td.getDescription() == null ? "" : td.getDescription().toLowerCase();
                return name.contains(ft) || desc.contains(ft);
            });
        }
    }

    private void gridRefresh(){
        rows.clear();
        rows.addAll(tableDetailService.getAllTables());
        dataProvider.refreshAll();
    }

    private void clearForm() {
        tableName.clear();
        tableDescription.clear();
        totalSeats.clear();
        availableSeats.clear();
        unavailableSeats.clear();
    }

    private boolean validateForm() {
        String name = tableName.getValue();
        Integer total = totalSeats.getValue();
        Integer avail = availableSeats.getValue();
        Integer unavail = unavailableSeats.getValue();

        if (name == null || name.isBlank()) {
            showError("Table name is required");
            return false;
        }
        if (total == null || avail == null || unavail == null) {
            showError("Please fill in all numeric fields");
            return false;
        }
        if (total < 0 || avail < 0 || unavail < 0) {
            showError("Values cannot be negative");
            return false;
        }
        if (avail + unavail != total) {
            showError("Available + Unavailable must equal Total seats");
            return false;
        }
        return true;
    }

    private void confirmAndDelete(TableDetail item) {
        // Ensure the right-clicked item is selected for user feedback
        grid.select(item);

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete table");
        dialog.setText("Are you sure you want to delete \"" + item.getTableName() + "\"?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            // Remove from data source (and service if applicable)
            try {
                tableDetailService.delete(item);
            } catch (Exception ex) {
                Notification n = Notification.show("Failed to delete: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            gridRefresh();
            Notification n = Notification.show("Deleted", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }


    private void showError(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }





}
