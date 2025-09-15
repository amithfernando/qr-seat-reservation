package com.amithfernando.qrseatreservation.ui.view;


import com.amithfernando.qrseatreservation.core.enums.Role;
import com.amithfernando.qrseatreservation.core.model.User;
import com.amithfernando.qrseatreservation.core.repsitory.UserRepository;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.Objects;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Users")
public class UserManageView extends PageLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Grid<User> grid = new Grid<>(User.class, false);

    private final TextField username = new TextField("Username");
    private final PasswordField newPassword = new PasswordField("New Password");
    private final ComboBox<Role> role = new ComboBox<>("Role");
    private final Checkbox enabled = new Checkbox("Enabled");

    private final Button newBtn = new Button("New", VaadinIcon.PLUS_CIRCLE.create());
    private final Button saveBtn = new Button("Save", VaadinIcon.CHECK.create());
    private final Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
    private final Button clearBtn = new Button("Clear", VaadinIcon.ERASER.create());

    // Dialog that holds the form
    private final Dialog formDialog = new Dialog();

    private User editing;

    public UserManageView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(VaadinIcon.USER, "Users");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        buildGrid();
        buildFormDialog();
        loadUsers();
    }

    private void buildGrid() {
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true).setSortable(true);
        grid.addColumn(u -> u.getRole() != null ? u.getRole().name() : "").setHeader("Role").setAutoWidth(true).setSortable(true);
        grid.addColumn(User::isEnabled).setHeader("Enabled").setAutoWidth(true).setSortable(true);
        grid.addColumn(u -> u.getCreatedAt() != null ? u.getCreatedAt().toString() : "")
                .setHeader("Created At").setAutoWidth(true).setSortable(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("45vh");

        grid.asSingleSelect().addValueChangeListener(e -> {
            User u = e.getValue();
            if (u == null) {
                return;
            }
            setEditing(u);
            formDialog.setHeaderTitle("Edit user");
            deleteBtn.setEnabled(true);
            formDialog.open();
        });

        // Toolbar with "New" button
        newBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBtn.addClickListener(e -> {
            clearForm();
            editing = new User();
            enabled.setValue(true);
            formDialog.setHeaderTitle("New user");
            deleteBtn.setEnabled(false);
            formDialog.open();
        });

        addToContent(new HorizontalLayout(newBtn), grid);
    }

    // Build the dialog and place the form inside it
    private void buildFormDialog() {
        role.setItems(Role.values());
        enabled.setValue(true);
        newPassword.setPlaceholder("Leave blank to keep current");

        username.setClearButtonVisible(true);
        newPassword.setClearButtonVisible(true);
        role.setClearButtonVisible(true);

        FormLayout form = new FormLayout(username, newPassword, role, enabled);
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formDialog.removeAll();
        formDialog.add(form);

        // Dialog actions
        Button cancelBtn = new Button("Cancel", ev -> formDialog.close());

        saveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickListener(e -> onSave());

        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> onDelete());

        clearBtn.addClickListener(e -> clearForm());

        HorizontalLayout footer = new HorizontalLayout(cancelBtn, clearBtn, saveBtn, deleteBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        formDialog.getFooter().removeAll();
        formDialog.getFooter().add(footer);

        // Dialog UX
        formDialog.setModal(true);
        formDialog.setDraggable(true);
        formDialog.setResizable(true);
        formDialog.setWidth("520px");

        // Add dialog to page so it can be opened later
        addToContent(formDialog);
    }

    private void setEditing(User u) {
        editing = u;
        username.setValue(Objects.toString(u.getUsername(), ""));
        role.setValue(u.getRole());
        enabled.setValue(u.isEnabled());
        newPassword.clear();
    }

    private void clearForm() {
        editing = null;
        username.clear();
        role.clear();
        enabled.setValue(true);
        newPassword.clear();
        grid.deselectAll();
    }

    private void onSave() {
        try {
            if (editing == null) {
                editing = new User();
            }
            if (username.isEmpty()) {
                notifyError("Username is required");
                return;
            }
            if (editing.getId() == null && newPassword.isEmpty()) {
                notifyError("Password is required for new users");
                return;
            }
            editing.setUsername(username.getValue().trim());
            editing.setRole(role.getValue() == null ? Role.ENTRANCE : role.getValue());
            editing.setEnabled(enabled.getValue());

            if (!newPassword.isEmpty()) {
                editing.setPassword(passwordEncoder.encode(newPassword.getValue()));
            } else if (editing.getId() == null) {
                notifyError("Password is required");
                return;
            }

            userRepository.save(editing);
            loadUsers();
            notifySuccess("Saved");
            formDialog.close();
            clearForm();
        } catch (Exception ex) {
            notifyError("Save failed: " + ex.getMessage());
        }
    }

    private void onDelete() {
        if (editing == null || editing.getId() == null) {
            notifyError("Select a user to delete");
            return;
        }
        try {
            userRepository.deleteById(editing.getId());
            loadUsers();
            notifySuccess("Deleted");
            formDialog.close();
            clearForm();
        } catch (Exception ex) {
            notifyError("Delete failed: " + ex.getMessage());
        }
    }

    private void loadUsers() {
        grid.setItems(userRepository.findAll());
    }

    private void notifySuccess(String msg) {
        Notification n = Notification.show(msg, 2000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyError(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
