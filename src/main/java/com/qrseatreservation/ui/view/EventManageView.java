package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.service.EventService;
import com.qrseatreservation.core.service.EventService.CopyOption;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manage ticket campaigns: create a new event (optionally copying setup from a previous one), switch the admin's
 * working event, activate a prepared event (archives the current one) and archive events.
 */
@Route(value = "events", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Events")
@Slf4j
public class EventManageView extends PageLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Map<CopyOption, String> COPY_LABELS = Map.of(
            CopyOption.TEMPLATE, "Ticket template & layout settings",
            CopyOption.CATEGORIES, "Ticket categories & prices",
            CopyOption.TABLES, "Tables, seats & floor layout",
            CopyOption.SELLERS, "Sellers (new portal links, same passwords)");

    private final EventService eventService;
    private final EventContext eventContext;
    private final Div list = new Div();

    public EventManageView(EventService eventService, EventContext eventContext) {
        super(VaadinIcon.CALENDAR_BRIEFCASE, "Events");
        this.eventService = eventService;
        this.eventContext = eventContext;

        Paragraph help = new Paragraph("Prepare an event as a draft, then activate it. Several events can be active "
                + "at the same time; every user works on the event they selected after login, and check-in only accepts "
                + "tickets of that event. Archive an event when it is over: it becomes read-only (kept for reports) and its "
                + "seller portal links stop working. Archived events are not offered after login; open them here. "
                + "Non-admin users only see events assigned to them in Users. "
                + "Ticket numbers are unique across all events, so old QR codes are never accepted at a new event.");
        help.addClassName("page-intro");

        Button create = new Button("New Event", VaadinIcon.PLUS.create(), e -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> refresh());
        HorizontalLayout toolbar = new HorizontalLayout(refresh, create);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        toolbar.setPadding(false);

        list.addClassName("event-list");
        addToContent(help, toolbar, list);
        refresh();
    }

    private Div card(Event ev) {
        Div card = new Div();
        card.addClassName("event-card");
        if (ev.isArchived()) card.addClassName("archived");
        if (isWorkingEvent(ev)) card.addClassName("working");
        card.getStyle().set("--poster-hue", String.valueOf(HomeView.posterHue(ev)));

        Div top = new Div();
        top.addClassName("event-card-top");
        Div heading = new Div();
        heading.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px").set("min-width", "0");
        Div chips = new Div();
        chips.addClassName("chip-row");
        chips.add(statusBadge(ev));
        if (isWorkingEvent(ev)) {
            Span you = new Span("Open in your session");
            you.addClassNames("chip", "cyan");
            chips.add(you);
        }
        H3 title = new H3(ev.getName());
        title.addClassName("event-card-title");
        heading.add(chips, title);
        top.add(heading);

        Div body = new Div();
        body.addClassName("event-card-body");
        Div facts = new Div();
        facts.addClassName("event-card-facts");
        if (ev.getEventDate() != null) facts.add(fact(VaadinIcon.CALENDAR, DATE.format(ev.getEventDate())));
        if (ev.getVenue() != null) facts.add(fact(VaadinIcon.MAP_MARKER, ev.getVenue()));
        facts.add(fact(VaadinIcon.TICKET, eventService.countTickets(ev.getId()) + " tickets"));
        if (ev.getCreatedAt() != null) facts.add(fact(VaadinIcon.CLOCK, "Created " + DATE_TIME.format(ev.getCreatedAt())));
        body.add(facts);

        HorizontalLayout actions = actions(ev);
        actions.addClassName("event-card-actions");
        card.add(top, body, actions);
        return card;
    }

    private static Span fact(VaadinIcon icon, String text) {
        Span s = new Span();
        s.addClassName("icon-text");
        s.add(icon.create(), new Span(text));
        return s;
    }

    private HorizontalLayout actions(Event ev) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);

        Button open = new Button("Open", VaadinIcon.SIGN_IN.create(), e -> {
            eventContext.select(ev.getId());
            UI.getCurrent().getPage().reload();
        });
        open.addThemeVariants(ButtonVariant.LUMO_SMALL);
        open.setEnabled(!isWorkingEvent(ev));
        open.setTooltipText("Work on this event in your session (other users are not affected)");
        row.add(open);

        if (!ev.isArchived()) {
            Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(ev));
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            edit.setTooltipText("Edit name, venue and date");
            row.add(edit);
        }
        if (ev.getStatus() == EventStatus.DRAFT) {
            Button activate = new Button("Activate", VaadinIcon.PLAY.create(), e -> confirmActivate(ev));
            activate.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            row.add(activate);
        }
        if (!ev.isArchived()) {
            Button archive = new Button("Archive", VaadinIcon.ARCHIVE.create(), e -> confirmArchive(ev));
            archive.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            row.add(archive);
        }
        return row;
    }

    private boolean isWorkingEvent(Event ev) {
        try {
            return ev.getId().equals(eventContext.currentId());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Span statusBadge(Event ev) {
        Span badge = new Span(ev.getStatus().name());
        badge.addClassNames("chip", switch (ev.getStatus()) {
            case ACTIVE -> "success";
            case DRAFT -> "accent";
            case ARCHIVED -> "danger";
        });
        return badge;
    }

    private void refresh() {
        list.removeAll();
        eventService.listEvents().forEach(ev -> list.add(card(ev)));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Event");
        dialog.setWidth("560px");

        TextField name = new TextField("Event name");
        name.setRequired(true);
        name.setMaxLength(255);
        name.setWidthFull();
        TextField venue = new TextField("Venue");
        venue.setMaxLength(255);
        venue.setWidthFull();
        DatePicker date = new DatePicker("Event date");
        date.setWidthFull();

        List<Event> events = eventService.listEvents();
        ComboBox<Event> source = new ComboBox<>("Copy setup from (optional)");
        source.setItems(events);
        source.setItemLabelGenerator(Event::getDisplayName);
        source.setClearButtonVisible(true);
        source.setWidthFull();
        events.stream().filter(Event::isActive).findFirst()
                .or(() -> events.stream().findFirst())
                .ifPresent(source::setValue);

        CheckboxGroup<CopyOption> copy = new CheckboxGroup<>("What to copy");
        copy.setItems(CopyOption.values());
        copy.setItemLabelGenerator(COPY_LABELS::get);
        copy.setValue(EnumSet.of(CopyOption.TEMPLATE, CopyOption.CATEGORIES, CopyOption.TABLES));
        copy.setEnabled(source.getValue() != null);
        source.addValueChangeListener(e -> copy.setEnabled(e.getValue() != null));

        Paragraph note = new Paragraph("The new event starts as a DRAFT. Tickets and reservations are never copied; "
                + "generate new tickets in the draft before activating it.");
        note.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout form = new VerticalLayout(name, venue, date, source, copy, note);
        form.setPadding(false);
        dialog.add(form);

        Button cancel = new Button("Cancel", e -> dialog.close());
        Button save = new Button("Create", e -> {
            if (name.getValue() == null || name.getValue().isBlank()) {
                name.setInvalid(true);
                name.setErrorMessage("Name is required");
                return;
            }
            run(() -> {
                Event created = eventService.createEvent(name.getValue(), venue.getValue(), date.getValue(),
                        source.getValue() != null ? source.getValue().getId() : null,
                        source.getValue() != null ? copy.getValue() : EnumSet.noneOf(CopyOption.class));
                dialog.close();
                success("Event '" + created.getName() + "' created as draft. Use 'Open' to set it up.");
                refresh();
            });
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
        name.focus();
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    private void openEditDialog(Event ev) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Event");
        dialog.setWidth("480px");
        TextField name = new TextField("Event name", ev.getName(), "");
        name.setRequired(true);
        name.setMaxLength(255);
        name.setWidthFull();
        TextField venue = new TextField("Venue", ev.getVenue() != null ? ev.getVenue() : "", "");
        venue.setMaxLength(255);
        venue.setWidthFull();
        DatePicker date = new DatePicker("Event date", ev.getEventDate());
        date.setWidthFull();
        VerticalLayout form = new VerticalLayout(name, venue, date);
        form.setPadding(false);
        dialog.add(form);
        Button cancel = new Button("Cancel", e -> dialog.close());
        Button save = new Button("Save", e -> run(() -> {
            eventService.updateDetails(ev.getId(), name.getValue(), venue.getValue(), date.getValue());
            dialog.close();
            success("Event updated.");
            refresh();
        }));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    // ── Activate / archive ────────────────────────────────────────────────────

    private void confirmActivate(Event ev) {
        List<String> active = eventService.listEvents().stream().filter(Event::isActive).map(Event::getName).toList();
        long tickets = eventService.countTickets(ev.getId());
        String text = "'" + ev.getName() + "' becomes ACTIVE: check-in is enabled and assigned users can select it."
                + (tickets == 0 ? " Warning: this event has no tickets generated yet." : " It has " + tickets + " ticket(s).")
                + (active.isEmpty() ? "" : " Currently active: " + String.join(", ", active) + " — they stay active unless you tick the box below.");
        Checkbox archiveOthers = new Checkbox("Also archive the currently active event(s) (read-only, cannot be undone)");
        archiveOthers.setVisible(!active.isEmpty());
        typedConfirm("Activate event", text, ev.getName(), "Activate", archiveOthers, ok -> run(() -> {
            eventService.activate(ev.getId(), archiveOthers.getValue());
            eventContext.select(ev.getId());
            success("'" + ev.getName() + "' is now active.");
            UI.getCurrent().getPage().reload();
        }));
    }

    private void confirmArchive(Event ev) {
        String text = "'" + ev.getName() + "' will be ARCHIVED. This cannot be undone: the event becomes read-only "
                + "(reports and exports remain available), check-in stops and seller portal links stop working.";
        typedConfirm("Archive event", text, ev.getName(), "Archive", null, ok -> run(() -> {
            eventService.archive(ev.getId());
            success("'" + ev.getName() + "' archived.");
            UI.getCurrent().getPage().reload();
        }));
    }

    /** Destructive actions require typing the event name, to avoid activating/archiving the wrong event by accident. */
    private void typedConfirm(String title, String text, String expected, String confirmLabel,
                              Component extra, Consumer<Void> onConfirm) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("520px");
        Paragraph p = new Paragraph(text);
        TextField typed = new TextField("Type the event name to confirm");
        typed.setPlaceholder(expected);
        typed.setWidthFull();
        VerticalLayout body = new VerticalLayout(p, typed);
        if (extra != null) body.add(extra);
        dialog.add(body);
        Button cancel = new Button("Cancel", e -> dialog.close());
        Button confirm = new Button(confirmLabel, e -> {
            dialog.close();
            onConfirm.accept(null);
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirm.setEnabled(false);
        typed.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        typed.addValueChangeListener(e -> confirm.setEnabled(expected.equals(e.getValue() != null ? e.getValue().trim() : "")));
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
        typed.focus();
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException | com.qrseatreservation.core.event.EventAccessException ex) {
            Notification n = Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (RuntimeException ex) {
            log.error("Event operation failed", ex);
            Notification n = Notification.show("Operation failed: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static void success(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
