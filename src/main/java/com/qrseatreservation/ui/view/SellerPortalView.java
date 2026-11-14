package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.service.ReservationService;
import com.qrseatreservation.core.service.SellerDetailService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@Route("seller-portal/:token")
@AnonymousAllowed
@PageTitle("Seller Portal")
@Slf4j
public class SellerPortalView extends VerticalLayout implements BeforeEnterObserver {

    private final SellerDetailService sellerDetailService;
    private final ReservationService  reservationService;

    private String token;
    private SellerDetail seller;

    public SellerPortalView(SellerDetailService sellerDetailService,
                            ReservationService reservationService) {
        this.sellerDetailService = sellerDetailService;
        this.reservationService  = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "var(--app-surface-muted, #f1f5fb)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = event.getRouteParameters().get("token").orElse("");
        if (token.isBlank()) {
            showInvalid();
            return;
        }

        sellerDetailService.findByAccessToken(token).ifPresentOrElse(s -> {
            this.seller = s;
            if (s.getPasswordHash() == null) {
                showInvalid();
            } else {
                showLoginForm();
            }
        }, this::showInvalid);
    }

    // ── Login form ────────────────────────────────────────────────────────────

    private void showLoginForm() {
        removeAll();

        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.12)")
                .set("padding", "2.5rem")
                .set("width", "100%")
                .set("max-width", "420px")
                .set("box-sizing", "border-box");

        Span eyebrow = new Span("Seller Portal");
        eyebrow.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.8rem").set("font-weight", "600").set("text-transform", "uppercase")
                .set("letter-spacing", "0.08em");

        H2 name = new H2(seller.getName() != null ? seller.getName() : "Seller");
        name.getStyle().set("margin", "0.25rem 0 0.1rem").set("font-size", "1.5rem").set("font-weight", "800");

        Span sub = new Span("Enter your password to view your ticket sales");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        PasswordField pwField = new PasswordField("Password");
        pwField.setWidthFull();
        pwField.setPlaceholder("Your portal password");

        Button loginBtn = new Button("View My Tickets", VaadinIcon.TICKET.create());
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.setWidthFull();
        loginBtn.addClickListener(e -> {
            if (sellerDetailService.verifyPassword(seller, pwField.getValue())) {
                showDashboard();
            } else {
                Notification n = Notification.show("Incorrect password.", 3000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                pwField.clear();
            }
        });
        // Allow Enter key to submit
        pwField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> loginBtn.click());

        VerticalLayout form = new VerticalLayout(eyebrow, name, sub, pwField, loginBtn);
        form.setPadding(false); form.setSpacing(true);
        card.add(form);

        VerticalLayout wrapper = new VerticalLayout(card);
        wrapper.setSizeFull();
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        wrapper.getStyle().set("padding", "2rem 1rem");
        add(wrapper);
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    private void showDashboard() {
        removeAll();

        List<ReservationDetail> reservations;
        try {
            reservations = reservationService.getReservationsForSeller(seller).stream()
                    .sorted(Comparator.comparing(ReservationDetail::getId).reversed())
                    .toList();
        } catch (com.qrseatreservation.core.event.EventAccessException ex) {
            showInvalid(); // the seller's event was archived meanwhile
            return;
        }

        // Summary counts
        long totalSeats   = reservations.stream().mapToLong(r -> r.getSeatReservations() == null ? 0 : r.getSeatReservations().size()).sum();
        long checkedIn    = reservations.stream().flatMap(r -> r.getSeatReservations() == null ? java.util.stream.Stream.empty() : r.getSeatReservations().stream())
                           .filter(sr -> sr.getReservationStatus() == ReservationStatus.CHECKED_IN).count();
        long paid         = reservations.stream().filter(r -> r.getReservationStatus() == ReservationStatus.PAID).count();

        // Header bar
        H3 title = new H3(seller.getName() + " — My Tickets");
        title.getStyle().set("margin", "0").set("font-size", "1.2rem").set("color", "#ffffff")
                .set("-webkit-text-fill-color", "#ffffff");

        Button logoutBtn = new Button("Logout", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        logoutBtn.getStyle().set("color", "rgba(255,255,255,0.85)");
        logoutBtn.addClickListener(e -> showLoginForm());

        HorizontalLayout header = new HorizontalLayout(title, logoutBtn);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.getStyle()
                .set("background", "linear-gradient(135deg,#4f46e5,#7c3aed)")
                .set("padding", "1rem 1.5rem");

        // Stat cards
        Div resCard    = statCard("Reservations",  String.valueOf(reservations.size()), "card-indigo");
        Div seatsCard  = statCard("Total Seats",   String.valueOf(totalSeats),          "card-teal");
        Div paidCard   = statCard("Paid",          String.valueOf(paid),                "card-emerald");
        Div checkinCard= statCard("Checked-in",    String.valueOf(checkedIn),           "card-amber");

        HorizontalLayout statsRow = new HorizontalLayout(resCard, seatsCard, paidCard, checkinCard);
        statsRow.setWidthFull(); statsRow.setSpacing(true);
        statsRow.getStyle().set("flex-wrap","wrap").set("padding","1rem 1.5rem 0");

        // Reservations grid
        Grid<ReservationDetail> grid = new Grid<>(ReservationDetail.class, false);
        grid.addColumn(ReservationDetail::getReferenceNo).setHeader("Reference #").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getReservationStatus() != null ? r.getReservationStatus().name() : "")
                .setHeader("Status").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getSeatReservations() == null ? "0" : String.valueOf(r.getSeatReservations().size()))
                .setHeader("Seats").setAutoWidth(true);
        grid.addColumn(ReservationDetail::getDescription).setHeader("Description").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(r -> {
            if (r.getSeatReservations() == null || r.getSeatReservations().isEmpty())
                return new Span("—");
            long ci = r.getSeatReservations().stream()
                    .filter(sr -> sr.getReservationStatus() == ReservationStatus.CHECKED_IN).count();
            Span s = new Span(ci + " / " + r.getSeatReservations().size() + " in");
            s.getStyle().set("color", ci == r.getSeatReservations().size()
                    ? "var(--lumo-success-text-color)" : "var(--lumo-secondary-text-color)");
            return s;
        })).setHeader("Check-ins").setAutoWidth(true);

        grid.getElement().getThemeList().add("row-stripes");
        grid.setWidthFull();
        grid.setHeight("45vh");
        grid.setItems(reservations);

        // Detail panel on row select — shows seat ticket numbers
        VerticalLayout seatPanel = new VerticalLayout();
        seatPanel.setWidthFull(); seatPanel.setPadding(false);

        grid.asSingleSelect().addValueChangeListener(ev -> {
            seatPanel.removeAll();
            ReservationDetail rd = ev.getValue();
            if (rd == null || rd.getSeatReservations() == null || rd.getSeatReservations().isEmpty()) return;

            Grid<SeatReservation> seatGrid = new Grid<>(SeatReservation.class, false);
            seatGrid.addColumn(SeatReservation::getTicketNo).setHeader("Ticket #").setAutoWidth(true);
            seatGrid.addColumn(sr -> sr.getSeatDetail() != null ? sr.getSeatDetail().getSeatNo() : "")
                    .setHeader("Seat").setAutoWidth(true);
            seatGrid.addColumn(sr -> sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                    ? sr.getSeatDetail().getTableDetail().getTableName() : "")
                    .setHeader("Table").setAutoWidth(true);
            seatGrid.addColumn(new ComponentRenderer<>(sr -> {
                String st = sr.getReservationStatus() != null ? sr.getReservationStatus().name() : "";
                Span badge = new Span(st);
                badge.getElement().getThemeList().add("badge");
                if (sr.getReservationStatus() == ReservationStatus.CHECKED_IN)
                    badge.getElement().getThemeList().add("success");
                return badge;
            })).setHeader("Status").setAutoWidth(true);
            seatGrid.addColumn(new ComponentRenderer<>(this::buyerNameField))
                    .setHeader("Buyer Name (for your tracking)").setAutoWidth(true).setFlexGrow(1);

            seatGrid.getElement().getThemeList().add("row-stripes");
            seatGrid.setWidthFull();
            seatGrid.setHeight("200px");
            seatGrid.setItems(rd.getSeatReservations());

            Span refLabel = new Span("Seats for reservation: " + rd.getReferenceNo());
            refLabel.getStyle().set("font-weight","700").set("font-size","0.9rem")
                    .set("color","var(--lumo-secondary-text-color)");
            seatPanel.add(refLabel, seatGrid);
        });

        VerticalLayout content = new VerticalLayout(statsRow, grid, seatPanel);
        content.setWidthFull(); content.setPadding(true); content.setSpacing(true);
        content.getStyle().set("max-width","1200px").set("margin","0 auto").set("box-sizing","border-box");

        add(header, content);
    }

    /** Inline, auto-saving buyer-name field for one seat row — saves on blur or Enter. */
    private TextField buyerNameField(SeatReservation sr) {
        TextField field = new TextField();
        field.setValue(sr.getBuyerName() != null ? sr.getBuyerName() : "");
        field.setPlaceholder("Add name…");
        field.setWidthFull();
        field.getStyle().set("--lumo-text-field-size", "var(--lumo-size-s)");

        Runnable save = () -> {
            String value = field.getValue();
            boolean ok = reservationService.updateBuyerName(seller, sr.getId(), value);
            if (ok) {
                sr.setBuyerName(value != null && !value.isBlank() ? value.trim() : null);
            } else {
                Notification n = Notification.show("Could not save — please refresh and try again.",
                        2500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };
        field.addBlurListener(e -> save.run());
        field.addKeyDownListener(Key.ENTER, e -> save.run());
        return field;
    }

    private static Div statCard(String label, String value, String colorClass) {
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size","0.72rem").set("font-weight","600")
                .set("text-transform","uppercase").set("letter-spacing","0.05em")
                .set("color","var(--lumo-secondary-text-color)");
        Span val = new Span(value);
        val.getStyle().set("font-size","2rem").set("font-weight","800")
                .set("letter-spacing","-0.03em").set("color","var(--lumo-body-text-color)");
        VerticalLayout inner = new VerticalLayout(lbl, val);
        inner.setPadding(false); inner.setSpacing(false);
        Div card = new Div(inner);
        card.addClassNames("stat-card", colorClass);
        card.getStyle().set("flex","1 1 140px").set("min-width","120px");
        return card;
    }

    // ── Invalid/error state ───────────────────────────────────────────────────

    private void showInvalid() {
        removeAll();
        Span msg = new Span("This portal link is invalid or has been revoked.");
        msg.getStyle().set("color","var(--lumo-secondary-text-color)");
        VerticalLayout w = new VerticalLayout(
                VaadinIcon.WARNING.create(), msg);
        w.setAlignItems(FlexComponent.Alignment.CENTER);
        w.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        w.setSizeFull();
        add(w);
    }
}
