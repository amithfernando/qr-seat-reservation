package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.service.DashboardService;
import com.qrseatreservation.core.service.DashboardService.*;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Event dashboard: hero card for the selected event, key figures, seat and ticket status, quick actions,
 * table availability, recent reservations and (admins) sales insights. Mobile first; layout is pure CSS grid.
 */
@Route(value = "/", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "ENTRANCE"})
@PageTitle("Home")
public class HomeView extends Div {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH);

    private final boolean admin = EventContext.isAdmin();

    public HomeView(DashboardService dashboardService) {
        addClassName("home");
        Snapshot s = dashboardService.snapshot();
        boolean readOnly = s.event().isArchived();

        add(hero(s, readOnly));
        add(kpis(s));
        if (admin && !readOnly) {
            Component setup = setupChecklist(s);
            if (setup != null) add(setup);
        }

        Div statusRow = div("home-grid two");
        statusRow.add(seatStatus(s.seats()), ticketInventory(s));
        add(statusRow);

        add(quickActions(readOnly, s.event()));
        if (!s.tableStats().isEmpty()) add(tablesCarousel(s));

        Div bottom = div("home-grid two");
        bottom.add(recent(s));
        if (admin) {
            Div insights = div("home-stack");
            insights.add(categories(s), topSellers(s));
            bottom.add(insights);
        } else {
            bottom.add(checkInProgress(s));
        }
        add(bottom);
    }

    // ── Hero ──────────────────────────────────────────────────────────────────

    private Component hero(Snapshot s, boolean readOnly) {
        Event ev = s.event();
        Div hero = div("home-hero");
        hero.getStyle().set("--poster-hue", String.valueOf(posterHue(ev)));

        Div info = div("home-hero-info");
        Div chips = div("chip-row");
        chips.add(statusChip(ev.getStatus()));
        if (s.daysToGo() != null) {
            long d = s.daysToGo();
            String txt = d > 1 ? "In " + d + " days" : d == 1 ? "Tomorrow" : d == 0 ? "Today" : "Ended " + (-d) + " days ago";
            chips.add(chip(txt, d == 0 ? "chip accent" : "chip"));
        }
        if (readOnly) chips.add(chip("Read-only", "chip danger"));

        H1 name = new H1(ev.getName());
        name.addClassName("home-hero-title");
        Div meta = div("home-hero-meta");
        if (ev.getVenue() != null) meta.add(iconText(VaadinIcon.MAP_MARKER, ev.getVenue()));
        if (ev.getEventDate() != null) meta.add(iconText(VaadinIcon.CALENDAR, DATE.format(ev.getEventDate())));

        Div actions = div("home-hero-actions");
        if (ev.isActive()) actions.add(cta("Scan tickets", VaadinIcon.QRCODE, CheckingView.class, "btn primary"));
        if (admin && !readOnly) actions.add(cta("New reservation", VaadinIcon.PLUS, ReservationManageView.class,
                ev.isActive() ? "btn ghost" : "btn primary"));
        info.add(chips, name, meta, actions);

        hero.add(info, ring(s.seats().occupancyPct(), "Occupancy",
                (s.seats().reserved() + s.seats().checkedIn()) + " / " + s.seats().sellable() + " seats"));
        return hero;
    }

    private Component ring(double pct, String label, String sub) {
        Div ring = div("ring");
        ring.getStyle().set("--pct", String.format(Locale.ROOT, "%.1f", Math.min(100, pct)));
        Div inner = div("ring-inner");
        Span value = new Span(String.format(Locale.ROOT, "%.0f%%", pct));
        value.addClassName("ring-value");
        Span l = new Span(label);
        l.addClassName("ring-label");
        inner.add(value, l);
        ring.add(inner);
        Div wrap = div("ring-wrap");
        Span subText = new Span(sub);
        subText.addClassName("ring-sub");
        wrap.add(ring, subText);
        return wrap;
    }

    // ── KPIs ──────────────────────────────────────────────────────────────────

    private Component kpis(Snapshot s) {
        Div grid = div("kpi-grid");
        grid.add(kpi(VaadinIcon.TICKET, "Tickets sold", String.valueOf(s.ticketsSold()),
                s.seats().sellable() > 0 ? "of " + s.seats().sellable() + " sellable seats" : "no seats yet", "cyan"));
        grid.add(kpi(VaadinIcon.CHECK_CIRCLE, "Checked in", String.valueOf(s.checkedInTickets()),
                String.format(Locale.ROOT, "%.0f%% of %d expected", s.checkInPct(), s.expectedGuests()), "green"));
        if (admin) {
            grid.add(kpi(VaadinIcon.WALLET, "Collected", money(s.money().collected()),
                    s.money().paidTickets() + " paid tickets", "violet"));
            grid.add(kpi(VaadinIcon.HOURGLASS, "Outstanding", money(s.money().outstanding()),
                    s.money().pendingTickets() + " unpaid tickets", "orange"));
        } else {
            grid.add(kpi(VaadinIcon.CIRCLE_THIN, "Free seats", String.valueOf(s.seats().available()),
                    s.tables() + " tables", "violet"));
            grid.add(kpi(VaadinIcon.HOURGLASS, "Unpaid tickets", String.valueOf(s.money().pendingTickets()),
                    "collect at the door", "orange"));
        }
        return grid;
    }

    private Component kpi(VaadinIcon icon, String label, String value, String sub, String tone) {
        Div card = div("kpi " + tone);
        Icon i = icon.create();
        i.addClassName("kpi-icon");
        Span l = new Span(label);
        l.addClassName("kpi-label");
        Span v = new Span(value);
        v.addClassName("kpi-value");
        Span sb = new Span(sub);
        sb.addClassName("kpi-sub");
        card.add(i, l, v, sb);
        return card;
    }

    // ── Setup checklist (shown until the event is ready) ─────────────────────

    private Component setupChecklist(Snapshot s) {
        boolean tickets = s.tickets().generated() > 0;
        boolean tables = s.tables() > 0;
        boolean sellers = s.sellers() > 0;
        boolean sales = s.reservations() > 0;
        if (tickets && tables && sellers && sales) return null;

        Div card = card("Get this event ready", VaadinIcon.LIST_OL);
        card.addClassName("setup");
        card.add(step(tickets, "Generate tickets", "Ticket Layout Editor → Generate", TicketLayoutEditorView.class));
        card.add(step(tables, "Add tables & seats", "Floor plan for seat selection", TableManageView.class));
        card.add(step(sellers, "Add sellers", "Who sells tickets for you", SellerManageView.class));
        card.add(step(sales, "Create the first reservation", "Assign seats and tickets", ReservationManageView.class));
        return card;
    }

    private Component step(boolean done, String title, String hint, Class<? extends Component> target) {
        Div row = div("step" + (done ? " done" : ""));
        Icon i = (done ? VaadinIcon.CHECK_CIRCLE : VaadinIcon.CIRCLE_THIN).create();
        i.addClassName("step-icon");
        Div text = div("step-text");
        Span t = new Span(title);
        t.addClassName("step-title");
        Span h = new Span(hint);
        h.addClassName("step-hint");
        text.add(t, h);
        row.add(i, text);
        if (!done) {
            Icon go = VaadinIcon.ANGLE_RIGHT.create();
            go.addClassName("step-go");
            row.add(go);
            clickable(row, target);
        }
        return row;
    }

    // ── Seat status / ticket inventory ────────────────────────────────────────

    private Component seatStatus(SeatStats seats) {
        Div card = card("Seat status", VaadinIcon.GRID_SMALL);
        if (seats.total() == 0) {
            card.add(empty("No tables yet. Add tables to start selling seats."));
            return card;
        }
        Div bar = div("seg-bar");
        bar.add(segment(seats.available(), seats.total(), "seg available"),
                segment(seats.reserved(), seats.total(), "seg reserved"),
                segment(seats.checkedIn(), seats.total(), "seg checked"),
                segment(seats.unavailable(), seats.total(), "seg blocked"));
        Div legend = div("legend");
        legend.add(legendItem("available", "Available", seats.available()),
                legendItem("reserved", "Reserved", seats.reserved()),
                legendItem("checked", "Checked in", seats.checkedIn()),
                legendItem("blocked", "Blocked", seats.unavailable()));
        card.add(bar, legend);
        clickable(card, SeatingLayoutView.class);
        return card;
    }

    private Component ticketInventory(Snapshot s) {
        TicketStats t = s.tickets();
        Div card = card("Ticket inventory", VaadinIcon.TICKET);
        if (t.generated() == 0) {
            card.add(empty(admin ? "No tickets generated yet. Generate them in the Ticket Layout Editor."
                    : "No tickets generated for this event yet."));
            if (admin) clickable(card, TicketLayoutEditorView.class);
            return card;
        }
        double pct = t.assigned() * 100.0 / t.generated();
        Div stats = div("mini-stats");
        stats.add(mini("Generated", t.generated()), mini("Assigned", t.assigned()), mini("Remaining", t.available()));
        Div progress = div("progress");
        Div fill = div("progress-fill");
        fill.getStyle().set("width", String.format(Locale.ROOT, "%.1f%%", pct));
        progress.add(fill);
        card.add(stats, progress);
        if (t.available() == 0) {
            card.add(warning("All tickets are assigned — generate more before the next reservation."));
        } else if (t.available() * 10 < t.generated()) {
            card.add(warning("Less than 10% of tickets left."));
        }
        if (s.money().invitationTickets() > 0) {
            Span inv = new Span(s.money().invitationTickets() + " invitation ticket(s) issued");
            inv.addClassName("muted");
            card.add(inv);
        }
        return card;
    }

    // ── Quick actions ─────────────────────────────────────────────────────────

    private Component quickActions(boolean readOnly, Event ev) {
        Div section = section("Quick actions");
        Div grid = div("action-grid");
        if (ev.isActive()) grid.add(tile(VaadinIcon.QRCODE, "Check in", CheckingView.class, "cyan"));
        grid.add(tile(VaadinIcon.LAYOUT, "Seating", SeatingLayoutView.class, "violet"));
        if (admin) {
            grid.add(tile(VaadinIcon.BAR_CHART, "Reports", ReportView.class, "green"));
            grid.add(tile(VaadinIcon.CALENDAR, "Reservations", ReservationManageView.class, "cyan"));
            grid.add(tile(VaadinIcon.TABLE, "Tables", TableManageView.class, "violet"));
            grid.add(tile(VaadinIcon.USERS, "Sellers", SellerManageView.class, "orange"));
            grid.add(tile(VaadinIcon.TICKET, "Ticket design", TicketLayoutEditorView.class, "green"));
            grid.add(tile(VaadinIcon.CALENDAR_BRIEFCASE, "Events", EventManageView.class, "cyan"));
        }
        section.add(grid);
        return section;
    }

    private Component tile(VaadinIcon icon, String label, Class<? extends Component> target, String tone) {
        Div tile = div("tile " + tone);
        Div ic = div("tile-icon");
        ic.add(icon.create());
        Span l = new Span(label);
        l.addClassName("tile-label");
        tile.add(ic, l);
        clickable(tile, target);
        return tile;
    }

    // ── Tables carousel ───────────────────────────────────────────────────────

    private Component tablesCarousel(Snapshot s) {
        Div section = section("Tables");
        Div rail = div("rail");
        s.tableStats().stream()
                .sorted((a, b) -> Long.compare(b.available(), a.available()))
                .limit(24)
                .forEach(t -> rail.add(tableCard(t)));
        section.add(rail);
        return section;
    }

    private Component tableCard(TableStat t) {
        Div card = div("table-card" + (t.available() == 0 ? " full" : ""));
        Span name = new Span(t.name());
        name.addClassName("table-name");
        Span free = new Span(t.available() == 0 ? "Full" : t.available() + " free");
        free.addClassNames("chip", t.available() == 0 ? "danger" : "success");
        Div dots = div("seat-dots");
        for (SeatStatus st : t.seats()) {
            dots.add(div("dot " + switch (st) {
                case AVAILABLE -> "available";
                case RESERVED -> "reserved";
                case CHECKED_IN -> "checked";
                case UNAVAILABLE -> "blocked";
            }));
        }
        Span total = new Span(t.total() + " seats");
        total.addClassName("muted");
        card.add(name, free, dots, total);
        clickable(card, SeatingLayoutView.class);
        return card;
    }

    // ── Recent reservations / insights ────────────────────────────────────────

    private Component recent(Snapshot s) {
        Div card = card("Recent reservations", VaadinIcon.CLOCK);
        if (s.recent().isEmpty()) {
            card.add(empty("No reservations yet."));
            return card;
        }
        for (RecentReservation r : s.recent()) {
            Div row = div("list-row");
            Div avatar = div("avatar");
            avatar.setText(initials(r.seller()));
            Div text = div("list-text");
            Span title = new Span(r.seller());
            title.addClassName("list-title");
            Span sub = new Span(r.seats() + (r.seats() == 1 ? " seat" : " seats")
                    + (r.tables().isBlank() ? "" : " · " + r.tables())
                    + (r.createdAt() != null ? " · " + DATE_TIME.format(r.createdAt()) : ""));
            sub.addClassName("list-sub");
            text.add(title, sub);
            row.add(avatar, text, reservationChip(r.status()));
            card.add(row);
        }
        if (admin) clickable(card, ReservationManageView.class);
        return card;
    }

    private Component categories(Snapshot s) {
        Div card = card("Sales by category", VaadinIcon.TAGS);
        if (s.categories().isEmpty()) {
            card.add(empty("No tickets sold yet."));
            return card;
        }
        long max = s.categories().stream().mapToLong(CategoryStat::tickets).max().orElse(1);
        for (CategoryStat c : s.categories()) {
            card.add(barRow(c.name(), c.tickets() + " · " + money(c.revenue()), c.tickets() * 100.0 / max));
        }
        return card;
    }

    private Component topSellers(Snapshot s) {
        Div card = card("Top sellers", VaadinIcon.TROPHY);
        if (s.topSellers().isEmpty()) {
            card.add(empty("No sales yet."));
            return card;
        }
        int rank = 1;
        for (SellerStat st : s.topSellers()) {
            Div row = div("list-row");
            Div badge = div("rank r" + rank);
            badge.setText(String.valueOf(rank++));
            Div text = div("list-text");
            Span title = new Span(st.name());
            title.addClassName("list-title");
            Span sub = new Span(st.tickets() + " tickets");
            sub.addClassName("list-sub");
            text.add(title, sub);
            Span amount = new Span(money(st.revenue()));
            amount.addClassName("list-amount");
            row.add(badge, text, amount);
            card.add(row);
        }
        clickable(card, ReportView.class);
        return card;
    }

    private Component checkInProgress(Snapshot s) {
        Div card = card("Door progress", VaadinIcon.QRCODE);
        card.add(ring(s.checkInPct(), "Arrived", s.checkedInTickets() + " of " + s.expectedGuests() + " guests"));
        if (s.event().isActive()) clickable(card, CheckingView.class);
        return card;
    }

    private Component barRow(String label, String value, double pct) {
        Div row = div("bar-row");
        Div head = div("bar-head");
        Span l = new Span(label);
        Span v = new Span(value);
        v.addClassName("muted");
        head.add(l, v);
        Div track = div("progress thin");
        Div fill = div("progress-fill");
        fill.getStyle().set("width", String.format(Locale.ROOT, "%.1f%%", pct));
        track.add(fill);
        row.add(head, track);
        return row;
    }

    // ── Small building blocks ─────────────────────────────────────────────────

    private static Div div(String classes) {
        Div d = new Div();
        for (String c : classes.split(" ")) if (!c.isBlank()) d.addClassName(c);
        return d;
    }

    private static Div card(String title, VaadinIcon icon) {
        Div card = div("panel");
        Div head = div("panel-head");
        Icon i = icon.create();
        H3 h = new H3(title);
        head.add(i, h);
        card.add(head);
        return card;
    }

    private static Div section(String title) {
        Div s = div("home-section");
        H3 h = new H3(title);
        h.addClassName("section-heading");
        s.add(h);
        return s;
    }

    private static Span chip(String text, String classes) {
        Span s = new Span(text);
        for (String c : classes.split(" ")) s.addClassName(c);
        return s;
    }

    private static Span statusChip(EventStatus status) {
        return chip(status.name(), switch (status) {
            case ACTIVE -> "chip success";
            case DRAFT -> "chip accent";
            case ARCHIVED -> "chip danger";
        });
    }

    private static Span reservationChip(ReservationStatus status) {
        if (status == null) return chip("—", "chip");
        return switch (status) {
            case PAID -> chip("Paid", "chip success");
            case CHECKED_IN -> chip("Checked in", "chip cyan");
            case PAYMENT_PENDING -> chip("Unpaid", "chip warning");
        };
    }

    private static Component iconText(VaadinIcon icon, String text) {
        Span s = new Span();
        s.addClassName("icon-text");
        s.add(icon.create(), new Span(text));
        return s;
    }

    private static Component segment(long value, long total, String classes) {
        Div seg = div(classes);
        seg.getStyle().set("flex-grow", String.valueOf(value)).set("display", value == 0 ? "none" : "block");
        seg.getElement().setAttribute("title", value + " of " + total);
        return seg;
    }

    private static Component legendItem(String tone, String label, long value) {
        Div item = div("legend-item");
        item.add(div("dot " + tone));
        Span l = new Span(label);
        Span v = new Span(String.valueOf(value));
        v.addClassName("legend-value");
        item.add(l, v);
        return item;
    }

    private static Component mini(String label, long value) {
        Div m = div("mini");
        Span v = new Span(String.valueOf(value));
        v.addClassName("mini-value");
        Span l = new Span(label);
        l.addClassName("mini-label");
        m.add(v, l);
        return m;
    }

    private static Component empty(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassName("empty");
        return p;
    }

    private static Component warning(String text) {
        Div w = div("warning-note");
        w.add(VaadinIcon.WARNING.create(), new Span(text));
        return w;
    }

    private static Component cta(String text, VaadinIcon icon, Class<? extends Component> target, String classes) {
        Div b = div(classes);
        b.add(icon.create(), new Span(text));
        clickable(b, target);
        return b;
    }

    /** Makes a block keyboard- and screen-reader-accessible and navigates on click/Enter. */
    private static void clickable(Div component, Class<? extends Component> target) {
        component.addClassName("clickable");
        component.getElement().setAttribute("role", "link");
        component.getElement().setAttribute("tabindex", "0");
        component.addClickListener(e -> UI.getCurrent().navigate(target));
        component.getElement().addEventListener("keydown", e -> UI.getCurrent().navigate(target))
                .setFilter("event.key === 'Enter'");
    }

    private static String money(double amount) {
        return String.format(Locale.ROOT, "€%,.2f", amount);
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "")).toUpperCase(Locale.ROOT);
    }

    /** Stable per-event colour for the poster-style hero. */
    static int posterHue(Event ev) {
        return (int) ((ev.getId() != null ? ev.getId() : 0) * 67 % 360);
    }
}
