package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.model.TicketCategory;
import com.qrseatreservation.core.service.TicketCategoryService;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "ticket-categories", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Ticket Categories")
public class TicketCategoryView extends PageLayout {

    private final TicketCategoryService ticketCategoryService;
    private final Grid<TicketCategory> grid = new Grid<>(TicketCategory.class, false);
    private final Binder<TicketCategory> binder = new Binder<>(TicketCategory.class);
    
    private Dialog formDialog;
    private TicketCategory editingCategory;
    
    // Form fields
    private TextField nameField;
    private NumberField priceField;
    private TextArea descriptionField;
    private Checkbox activeCheckbox;

    public TicketCategoryView(TicketCategoryService ticketCategoryService) {
        super(VaadinIcon.TAGS, "Ticket Categories");
        this.ticketCategoryService = ticketCategoryService;
        
        configureGrid();
        configureFormDialog();
        createToolbar();

        VerticalLayout content = new VerticalLayout(grid);
        content.setSizeFull();
        content.setPadding(false);
        add(content);

        try {
            // Initialize default categories if none exist
            ticketCategoryService.initializeDefaultCategories();
            refreshGrid();
        } catch (Exception ex) {
            com.vaadin.flow.component.notification.Notification.show(
                    "Database connection error — please refresh the page.",
                    4000,
                    com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        }
    }

    private void createToolbar() {
        Button addButton = new Button("Add Category", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(null));
        
        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshGrid());
        
        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        toolbar.setPadding(false);
        
        add(toolbar);
    }

    private void configureGrid() {
        grid.addClassName("ticket-category-grid");
        grid.setSizeFull();
        
        grid.addColumn(TicketCategory::getName)
                .setHeader("Category Name")
                .setSortable(true)
                .setFlexGrow(1);
        
        grid.addColumn(category -> String.format("€%.2f", category.getPrice()))
                .setHeader("Price")
                .setSortable(true)
                .setFlexGrow(1);
        
        grid.addColumn(TicketCategory::getDescription)
                .setHeader("Description")
                .setFlexGrow(2);
        
        grid.addColumn(new ComponentRenderer<>(category -> {
            Span badge = new Span(category.getActive() ? "Active" : "Inactive");
            badge.getElement().getThemeList().add(
                category.getActive() ? "badge success" : "badge contrast"
            );
            badge.getStyle()
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", category.getActive() ? "var(--lumo-success-color-10pct)" : "var(--lumo-contrast-10pct)")
                .set("color", category.getActive() ? "var(--lumo-success-text-color)" : "var(--lumo-secondary-text-color)");
            return badge;
        }))
                .setHeader("Status")
                .setFlexGrow(1);
        
        grid.addColumn(new ComponentRenderer<>(category -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openFormDialog(category));
            
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> deleteCategory(category));
            
            return new HorizontalLayout(editBtn, deleteBtn);
        }))
                .setHeader("Actions")
                .setFlexGrow(1);
    }

    private void configureFormDialog() {
        formDialog = new Dialog();
        formDialog.setHeaderTitle("Ticket Category");
        formDialog.setModal(true);
        formDialog.setDraggable(true);
        formDialog.setResizable(true);
        
        // Form fields
        nameField = new TextField("Category Name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setPlaceholder("e.g., FULL, HALF, VIP");
        nameField.setWidthFull();
        
        priceField = new NumberField("Price");
        priceField.setRequiredIndicatorVisible(true);
        priceField.setPrefixComponent(new Span("€"));
        priceField.setMin(0);
        priceField.setStep(0.01);
        priceField.setWidthFull();
        
        descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Optional description");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(255);
        
        activeCheckbox = new Checkbox("Active");
        activeCheckbox.setValue(true);
        
        // Binder configuration
        binder.forField(nameField)
                .asRequired("Category name is required")
                .bind(TicketCategory::getName, TicketCategory::setName);
        
        binder.forField(priceField)
                .asRequired("Price is required")
                .withValidator(price -> price >= 0, "Price must be positive or zero")
                .bind(TicketCategory::getPrice, TicketCategory::setPrice);
        
        binder.bind(descriptionField, TicketCategory::getDescription, TicketCategory::setDescription);
        binder.bind(activeCheckbox, TicketCategory::getActive, TicketCategory::setActive);
        
        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, priceField, descriptionField, activeCheckbox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(descriptionField, 2);
        
        VerticalLayout dialogLayout = new VerticalLayout(formLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "500px").set("max-width", "100%");
        
        formDialog.add(dialogLayout);
        
        // Buttons
        Button saveButton = new Button("Save", e -> saveCategory());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Cancel", e -> formDialog.close());
        
        formDialog.getFooter().add(cancelButton, saveButton);
    }

    private void openFormDialog(TicketCategory category) {
        editingCategory = category;
        
        if (category == null) {
            // Add new category
            formDialog.setHeaderTitle("Add Ticket Category");
            binder.readBean(TicketCategory.builder()
                    .active(true)
                    .price(0.0)
                    .build());
        } else {
            // Edit existing category
            formDialog.setHeaderTitle("Edit Ticket Category");
            binder.readBean(category);
        }
        
        formDialog.open();
    }

    private void saveCategory() {
        try {
            TicketCategory category = editingCategory != null ? editingCategory : new TicketCategory();
            binder.writeBean(category);
            
            ticketCategoryService.saveCategory(category);
            
            Notification notification = Notification.show(
                    "Category saved successfully",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            formDialog.close();
            refreshGrid();
            
        } catch (ValidationException e) {
            Notification notification = Notification.show(
                    "Please check the form for errors",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            Notification notification = Notification.show(
                    e.getMessage(),
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteCategory(TicketCategory category) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Delete Category");
        
        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("Are you sure you want to delete the category '" + category.getName() + "'?"));
        layout.add(new Span("This action cannot be undone."));
        
        Button confirmButton = new Button("Delete", e -> {
            try {
                ticketCategoryService.deleteCategory(category.getId());
                
                Notification notification = Notification.show(
                        "Category deleted successfully",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                refreshGrid();
                confirmDialog.close();
                
            } catch (Exception ex) {
                Notification notification = Notification.show(
                        "Error deleting category: " + ex.getMessage(),
                        3000,
                        Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        
        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
        
        confirmDialog.add(layout);
        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    private void refreshGrid() {
        grid.setItems(ticketCategoryService.getAllCategories());
    }
}
