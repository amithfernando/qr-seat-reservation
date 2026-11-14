package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.service.SellerDetailService;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
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

    // Edit state
    private SellerDetail editingSeller = null;

    // Portal password dialog
    private final Dialog portalDialog = new Dialog();
    private final PasswordField portalPasswordField   = new PasswordField("New password");
    private final PasswordField portalConfirmField    = new PasswordField("Confirm password");
    private final Span         portalLinkSpan         = new Span();
    private SellerDetail portalSeller = null;

    private final SellerDetailService sellerDetailService;

    public SellerManageView(SellerDetailService sellerDetailService) {
        super(VaadinIcon.USERS,"Seller Settings");
        this.sellerDetailService = sellerDetailService;
        setSizeFull();
        initGrid();
        initDialog();
        initPortalDialog();
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

        // Portal status + manage button
        grid.addColumn(new ComponentRenderer<>(seller -> {
            boolean hasPortal = seller.getAccessToken() != null && seller.getPasswordHash() != null;
            Span badge = new Span(hasPortal ? "Active" : "None");
            badge.getElement().getThemeList().add(hasPortal ? "badge success" : "badge contrast");
            Button manageBtn = new Button(hasPortal ? VaadinIcon.LOCK.create() : VaadinIcon.UNLOCK.create());
            manageBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            manageBtn.addClickListener(e -> openPortalDialog(seller));
            manageBtn.getElement().setAttribute("title", "Manage portal access");
            HorizontalLayout cell = new HorizontalLayout(badge, manageBtn);
            cell.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
            cell.setPadding(false); cell.setSpacing(true);
            return cell;
        })).setHeader("Seller Portal").setAutoWidth(true);
        grid.setItems(dataProvider);

        // Visual and interaction improvements
        grid.getElement().getThemeList().add("row-stripes");
        grid.getElement().getThemeList().add("wrap-cell-content");
        grid.setColumnReorderingAllowed(true);

        rows.addAll(sellerDetailService.getAllSellers());

        // Context menu with Edit and Delete
        GridContextMenu<SellerDetail> contextMenu = grid.addContextMenu();
        GridMenuItem<SellerDetail> editItem = contextMenu.addItem("Edit", event -> {
            event.getItem().ifPresent(this::openEditDialog);
        });
        contextMenu.addItem(new com.vaadin.flow.component.html.Hr());
        GridMenuItem<SellerDetail> deleteItem = contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::confirmAndDelete);
        });
        contextMenu.addGridContextMenuOpenedListener(e -> {
            editItem.setEnabled(e.getItem() != null);
            deleteItem.setEnabled(e.getItem() != null);
        });
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
            editingSeller = null;
            dialog.setHeaderTitle("Add seller");
            clearForm();
            dialog.open();
        });

        saveButton.addClickListener(e -> {
            if (!validateForm()) {
                return;
            }
            if (editingSeller != null) {
                sellerDetailService.updateSeller(editingSeller,
                        sellerName.getValue().trim(),
                        sellerAddress.getValue(),
                        sellerEmail.getValue(),
                        sellerPhone.getValue().trim(),
                        sellerDescription.getValue());
                Notification.show("Seller updated", 2500, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                sellerDetailService.createSeller(
                        sellerName.getValue().trim(),
                        sellerAddress.getValue(),
                        sellerEmail.getValue(),
                        sellerPhone.getValue().trim(),
                        sellerDescription.getValue()
                );
                Notification.show("Seller added", 2500, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            editingSeller = null;
            gridRefresh();
            dialog.close();
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

    private void openEditDialog(SellerDetail item) {
        editingSeller = item;
        dialog.setHeaderTitle("Edit seller");
        sellerName.setValue(item.getName() != null ? item.getName() : "");
        sellerAddress.setValue(item.getAddress() != null ? item.getAddress() : "");
        sellerEmail.setValue(item.getEmail() != null ? item.getEmail() : "");
        sellerPhone.setValue(item.getPhone() != null ? item.getPhone() : "");
        sellerDescription.setValue(item.getDescription() != null ? item.getDescription() : "");
        dialog.open();
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

    // ── Seller Portal dialog ──────────────────────────────────────────────────

    private void initPortalDialog() {
        portalDialog.setHeaderTitle("Seller Portal Access");
        portalDialog.setModal(true);
        portalDialog.setDraggable(true);
        portalDialog.setWidth("480px");

        portalPasswordField.setWidthFull();
        portalConfirmField.setWidthFull();
        portalLinkSpan.getStyle().set("font-size","0.85rem").set("word-break","break-all")
                .set("color","var(--lumo-primary-text-color)");

        Button savePassBtn = new Button("Set Password", VaadinIcon.CHECK.create());
        savePassBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        savePassBtn.addClickListener(e -> onSavePortalPassword());

        Button revokeBtn = new Button("Revoke Access", VaadinIcon.TRASH.create());
        revokeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        revokeBtn.addClickListener(e -> {
            if (portalSeller == null) return;
            sellerDetailService.revokePortalAccess(portalSeller);
            Notification.show("Portal access revoked.", 2000, Notification.Position.BOTTOM_END);
            portalDialog.close();
            refreshGrid();
        });

        Button closeBtn = new Button("Close", e -> portalDialog.close());

        VerticalLayout content = new VerticalLayout(portalPasswordField, portalConfirmField, portalLinkSpan);
        content.setPadding(false); content.setSpacing(true);
        portalDialog.removeAll();
        portalDialog.add(content);
        portalDialog.getFooter().removeAll();
        portalDialog.getFooter().add(new HorizontalLayout(closeBtn, revokeBtn, savePassBtn));
    }

    private void openPortalDialog(SellerDetail seller) {
        portalSeller = seller;
        portalPasswordField.clear();
        portalConfirmField.clear();
        portalDialog.setHeaderTitle("Portal — " + seller.getName());

        if (seller.getAccessToken() != null) {
            String baseUrl = getBaseUrl();
            portalLinkSpan.setText("Portal link: " + baseUrl + "seller-portal/" + seller.getAccessToken());
        } else {
            portalLinkSpan.setText("No portal access yet. Set a password to generate a link.");
        }

        // Copy link button inline
        portalDialog.open();
    }

    private void onSavePortalPassword() {
        String pw  = portalPasswordField.getValue();
        String pw2 = portalConfirmField.getValue();
        if (pw.isBlank()) { showError("Password cannot be empty."); return; }
        if (!pw.equals(pw2)) { showError("Passwords do not match."); return; }
        if (pw.length() < 4) { showError("Password must be at least 4 characters."); return; }

        String token = sellerDetailService.setPortalPassword(portalSeller, pw);
        String baseUrl = getBaseUrl();
        String link = baseUrl + "seller-portal/" + token;
        portalLinkSpan.setText("Portal link: " + link);

        // Copy to clipboard
        getUI().ifPresent(ui -> ui.getPage().executeJs("navigator.clipboard?.writeText($0)", link));

        Notification n = Notification.show("Password saved. Link copied to clipboard.", 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        portalPasswordField.clear(); portalConfirmField.clear();
        refreshGrid();
    }

    /** Returns the current request base URL (e.g. https://host/). */
    private String getBaseUrl() {
        try {
            var req = com.vaadin.flow.server.VaadinServletRequest.getCurrent();
            if (req != null) {
                String url = req.getRequestURL().toString();
                int idx = url.indexOf("://");
                if (idx >= 0) {
                    int slash = url.indexOf("/", idx + 3);
                    return slash >= 0 ? url.substring(0, slash) + "/" : url + "/";
                }
            }
        } catch (Exception ignored) {}
        return "/";
    }

    private void refreshGrid() {
        rows.clear();
        rows.addAll(sellerDetailService.getAllSellers());
        dataProvider.refreshAll();
    }
}
