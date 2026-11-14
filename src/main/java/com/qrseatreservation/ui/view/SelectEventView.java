package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shown after login (and via "Switch event"): the user picks one of the events they may access.
 * Archived events are never listed here; admins can open those read-only from the Events page.
 */
@Route("select-event")
@PermitAll
@PageTitle("Select Event")
public class SelectEventView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", java.util.Locale.ENGLISH);

    private final EventContext eventContext;

    public SelectEventView(EventContext eventContext) {
        this.eventContext = eventContext;
        addClassName("select-event");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        Div brand = new Div();
        brand.addClassName("brand");
        Div mark = new Div(VaadinIcon.TICKET.create());
        mark.addClassName("brand-mark");
        Span brandName = new Span("QR Seat");
        brandName.addClassName("brand-name");
        brand.add(mark, brandName);

        H2 title = new H2("Choose your event");
        title.addClassName("select-title");
        Span sub = new Span("Signed in as " + EventContext.currentUser());
        sub.addClassName("select-sub");

        Div grid = new Div();
        grid.addClassName("poster-grid");
        List<Event> events = eventContext.selectableEvents();
        Long currentId = eventContext.hasValidSelection() ? eventContext.currentId() : null;
        for (Event ev : events) {
            grid.add(posterCard(ev, ev.getId().equals(currentId)));
        }

        Div content = new Div();
        content.addClassName("select-content");
        content.add(brand, title, sub);
        if (events.isEmpty()) {
            Paragraph none = new Paragraph("No events are assigned to your account. Please contact an administrator.");
            none.addClassName("select-empty");
            content.add(none);
        } else {
            content.add(grid);
        }

        Div footer = new Div();
        footer.addClassName("select-footer");
        if (EventContext.isAdmin()) {
            Button manage = new Button("Manage events", VaadinIcon.COG.create(),
                    e -> UI.getCurrent().navigate(EventManageView.class));
            manage.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            footer.add(manage);
        }
        Button logout = new Button("Sign out", VaadinIcon.SIGN_OUT.create(), e -> logout());
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        footer.add(logout);
        content.add(footer);
        add(content);
    }

    private Div posterCard(Event ev, boolean current) {
        Div card = new Div();
        card.addClassName("poster");
        if (current) card.addClassName("current");
        card.getStyle().set("--poster-hue", String.valueOf(HomeView.posterHue(ev)));
        card.getElement().setAttribute("role", "button");
        card.getElement().setAttribute("tabindex", "0");
        card.getElement().setAttribute("aria-label", "Open " + ev.getName());

        Div art = new Div();
        art.addClassName("poster-art");
        Span status = new Span(ev.getStatus().name());
        status.addClassNames("chip", ev.isActive() ? "success" : "accent");
        Icon icon = VaadinIcon.TICKET.create();
        icon.addClassName("poster-icon");
        art.add(status, icon);
        if (current) {
            Span cur = new Span("Current");
            cur.addClassNames("chip", "cyan", "poster-current");
            art.add(cur);
        }

        Div body = new Div();
        body.addClassName("poster-body");
        Span name = new Span(ev.getName());
        name.addClassName("poster-title");
        body.add(name);
        if (ev.getEventDate() != null) body.add(meta(VaadinIcon.CALENDAR, DATE.format(ev.getEventDate())));
        if (ev.getVenue() != null) body.add(meta(VaadinIcon.MAP_MARKER, ev.getVenue()));

        card.add(art, body);
        card.addClickListener(e -> choose(ev));
        card.getElement().addEventListener("keydown", e -> choose(ev)).setFilter("event.key === 'Enter' || event.key === ' '");
        return card;
    }

    private static Span meta(VaadinIcon icon, String text) {
        Span s = new Span();
        s.addClassName("poster-meta");
        s.add(icon.create(), new Span(text));
        return s;
    }

    private void choose(Event ev) {
        try {
            eventContext.select(ev.getId());
            UI.getCurrent().getPage().setLocation("/");
        } catch (EventAccessException ex) {
            Notification n = Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse(),
                    auth);
        }
        UI.getCurrent().getPage().setLocation("/login");
    }
}
