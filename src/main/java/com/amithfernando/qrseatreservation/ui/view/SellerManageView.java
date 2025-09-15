package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.service.SellerDetailService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value="sellerManage", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Seller Settings")
public class SellerManageView extends PageLayout {

    // Grid state
    private final Grid<SellerDetail> grid = new Grid<>(SellerDetail.class, false);
    private final List<SellerDetail> rows = new ArrayList<>();
    private final ListDataProvider<SellerDetail> dataProvider = new ListDataProvider<>(rows);

    // Toolbar
    private final TextField searchField = new TextField();

    // Dialog and form fields
    private final Dialog dialog = new Dialog();
    private final TextField sellerName = new TextField("Name");
    private final TextField sellerAddress = new TextField("Address");
    private final TextField sellerEmail = new TextField("Email");
    private final TextField sellerPhone = new TextField("Phone Number");
    private final TextField sellerDescription = new TextField("Description");
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    // Open dialog button
    private final Button openDialogButton = new Button("Add seller");

    private final SellerDetailService sellerDetailService;

    public SellerManageView(SellerDetailService sellerDetailService) {
        super(VaadinIcon.USERS,"Seller Settings");
        this.sellerDetailService = sellerDetailService;
        setSizeFull();
        initGrid();
        initDialog();
        initActions();
        setContents();
    }

    private void setContents() {
        // Toolbar with primary action and search
        searchField.setPlaceholder("Search sellers...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.addValueChangeListener(e -> applyFilter(e.getValue()));

        openDialogButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(openDialogButton, searchField);
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setAlignItems(Alignment.END);
        toolbar.getStyle().set("padding-bottom", "var(--lumo-space-s)");

        grid.setWidthFull();
        grid.setHeight("60vh");

        addToContent(toolbar, grid, dialog);
    }

    private void initGrid() {
        grid.addColumn(SellerDetail::getName).setHeader("Name").setAutoWidth(true).setSortable(true).setFlexGrow(1);
        grid.addColumn(SellerDetail::getAddress).setHeader("Address").setSortable(true).setFlexGrow(2);
        grid.addColumn(SellerDetail::getEmail).setHeader("Email").setAutoWidth(true).setSortable(true);
        grid.addColumn(SellerDetail::getPhone).setHeader("Phone Number").setAutoWidth(true).setSortable(true);
        grid.addColumn(SellerDetail::getDescription).setHeader("Description").setSortable(true).setFlexGrow(2);
        grid.setItems(dataProvider);

        // Visual and interaction improvements
        grid.getElement().getThemeList().add("row-stripes");
        grid.getElement().getThemeList().add("wrap-cell-content");
        grid.setColumnReorderingAllowed(true);

        rows.addAll(sellerDetailService.getAllSellers());

        // Context menu with Delete action
        GridContextMenu<SellerDetail> contextMenu = grid.addContextMenu();
        GridMenuItem<SellerDetail> deleteItem = contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::confirmAndDelete);
        });
        contextMenu.addGridContextMenuOpenedListener(e -> deleteItem.setEnabled(e.getItem() != null));
    }

    private void initDialog() {
        dialog.setHeaderTitle("Add seller");

        // Field configuration and UX
        sellerName.setRequiredIndicatorVisible(true);
        sellerName.setClearButtonVisible(true);
        sellerName.setWidthFull();

        sellerAddress.setClearButtonVisible(true);
        sellerAddress.setPlaceholder("Optional");
        sellerAddress.setWidthFull();

        sellerEmail.setClearButtonVisible(true);
        sellerEmail.setPlaceholder("name@example.com");
        sellerEmail.setWidthFull();

        sellerPhone.setRequiredIndicatorVisible(true);
        sellerPhone.setClearButtonVisible(true);
        sellerPhone.setPlaceholder("+1 555 123 4567");
        sellerPhone.setWidthFull();

        sellerDescription.setClearButtonVisible(true);
        sellerDescription.setPlaceholder("Notes or remarks (optional)");
        sellerDescription.setWidthFull();

        FormLayout form = new FormLayout(sellerName, sellerAddress, sellerEmail, sellerPhone, sellerDescription);
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", 2)
        );
        form.setColspan(sellerName, 1);
        form.setColspan(sellerAddress, 1);
        form.setColspan(sellerEmail, 1);
        form.setColspan(sellerPhone, 1);
        form.setColspan(sellerDescription, 2);

        dialog.add(form);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addClickListener(e -> dialog.close());
        dialog.getFooter().removeAll();
        dialog.getFooter().add(new HorizontalLayout(cancelButton, saveButton));

        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setWidth("560px");
    }

    private void initActions() {
        openDialogButton.addClickListener(e -> {
            clearForm();
            dialog.open();
        });

        saveButton.addClickListener(e -> {
            if (!validateForm()) {
                return;
            }
            sellerDetailService.createSeller(
                    sellerName.getValue().trim(),
                    sellerAddress.getValue(),
                    sellerEmail.getValue(),
                    sellerPhone.getValue().trim(),
                    sellerDescription.getValue()
            );
            gridRefresh();
            dialog.close();
            Notification.show("Seller added", 2500, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private void applyFilter(String filterText) {
        String ft = filterText == null ? "" : filterText.trim().toLowerCase();
        dataProvider.clearFilters();
        if (!ft.isEmpty()) {
            dataProvider.addFilter(sd -> {
                String name = sd.getName() == null ? "" : sd.getName().toLowerCase();
                String addr = sd.getAddress() == null ? "" : sd.getAddress().toLowerCase();
                String email = sd.getEmail() == null ? "" : sd.getEmail().toLowerCase();
                String phone = sd.getPhone() == null ? "" : sd.getPhone().toLowerCase();
                String desc = sd.getDescription() == null ? "" : sd.getDescription().toLowerCase();
                return name.contains(ft) || addr.contains(ft) || email.contains(ft) || phone.contains(ft) || desc.contains(ft);
            });
        }
    }

    private void gridRefresh(){
        rows.clear();
        rows.addAll(sellerDetailService.getAllSellers());
        dataProvider.refreshAll();
    }

    private void clearForm() {
        sellerName.clear();
        sellerAddress.clear();
        sellerEmail.clear();
        sellerPhone.clear();
        sellerDescription.clear();
    }

    private boolean validateForm() {
        String name = sellerName.getValue();
        String phoneNumber = sellerPhone.getValue();

        if (name == null || name.isBlank()) {
            showError("Seller name is required");
            return false;
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            showError("Phone number is required");
            return false;
        }
        return true;
    }

    private void confirmAndDelete(SellerDetail item) {
        // Ensure the right-clicked item is selected for user feedback
        grid.select(item);

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete seller");
        dialog.setText("Are you sure you want to delete \"" + item.getName() + "\"?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            // Remove from data source (and service if applicable)
            try {
                sellerDetailService.delete(item);
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
