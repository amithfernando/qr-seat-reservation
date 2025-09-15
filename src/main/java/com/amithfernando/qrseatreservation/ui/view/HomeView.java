package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.service.ReservationService;
import com.amithfernando.qrseatreservation.core.service.SellerDetailService;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.List;
import com.amithfernando.qrseatreservation.core.enums.SeatStatus;
import jakarta.annotation.security.RolesAllowed;

@Route(value="/", layout = MainLayout.class)
@RolesAllowed({"ADMIN","ENTRANCE"})
@PageTitle("Home")
public class HomeView extends PageLayout {

    private final ReservationService reservationService;
    private final TableDetailService tableDetailService;
    private final SellerDetailService sellerDetailService;

    public HomeView(ReservationService reservationService,
                    TableDetailService tableDetailService,
                    SellerDetailService sellerDetailService) {
        super(VaadinIcon.HOME,"Home");
        this.reservationService = reservationService;
        this.tableDetailService = tableDetailService;
        this.sellerDetailService = sellerDetailService;

        addContent();
    }

    private void addContent() {
        // Stats cards
        Div reservationsCard = statCard(VaadinIcon.CALENDAR, "Reservations",
                String.valueOf(reservationService.getAllReservations().size()));
        Div tablesCard = statCard(VaadinIcon.TABLE, "Tables",
                String.valueOf(tableDetailService.getAllTables().size()));
        Div sellersCard = statCard(VaadinIcon.USERS, "Sellers",
                String.valueOf(sellerDetailService.getAllSellers().size()));

        HorizontalLayout statsRow = new HorizontalLayout(reservationsCard, tablesCard, sellersCard);
        statsRow.setWidthFull();
        statsRow.setSpacing(true);
        statsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Seat summary cards (Available, Reserved, Checked-in)
        long availableSeats = tableDetailService.getAllTables().stream()
                .flatMap(t -> t.getSeatDetails().stream())
                .filter(sd -> sd.getSeatStatus() == SeatStatus.AVAILABLE)
                .count();

        long reservedSeats = tableDetailService.getAllTables().stream()
                .flatMap(t -> t.getSeatDetails().stream())
                .filter(sd -> sd.getSeatStatus() == SeatStatus.RESERVED)
                .count();

        long checkedInSeats = tableDetailService.getAllTables().stream()
                .flatMap(t -> t.getSeatDetails().stream())
                .filter(sd -> sd.getSeatStatus() == SeatStatus.CHECKED_IN)
                .count();

        Div availableCard = statCard(VaadinIcon.CHECK_CIRCLE, "Available seats", String.valueOf(availableSeats));
        Div reservedCard = statCard(VaadinIcon.BOOKMARK, "Reserved seats", String.valueOf(reservedSeats));
        Div checkedInCard = statCard(VaadinIcon.CHECK_SQUARE, "Checked-in seats", String.valueOf(checkedInSeats));

        HorizontalLayout seatStatsRow = new HorizontalLayout(availableCard, reservedCard, checkedInCard);
        seatStatsRow.setWidthFull();
        seatStatsRow.setSpacing(true);
        seatStatsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Quick actions
        Button newReservation = new Button("New Reservation", new Icon(VaadinIcon.PLUS_CIRCLE));
        newReservation.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newReservation.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ReservationManageView.class)));

        Button checkIn = new Button("Check In", new Icon(VaadinIcon.QRCODE));
        checkIn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        checkIn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(CheckingView.class)));

        Button seatingLayout = new Button("Seating Layout", new Icon(VaadinIcon.LAYOUT));
        seatingLayout.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SeatingLayoutView.class)));

        HorizontalLayout quickActions = new HorizontalLayout(newReservation, checkIn, seatingLayout);
        quickActions.setSpacing(true);
        quickActions.getStyle().set("margin-top", "var(--lumo-space-m)");
        quickActions.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        // Recent reservations
        Grid<ReservationDetail> recentGrid = new Grid<>(ReservationDetail.class, false);
        recentGrid.addColumn(ReservationDetail::getReferenceNo).setHeader("Reference #").setAutoWidth(true);
        recentGrid.addColumn(ReservationDetail::getReservationStatus).setHeader("Status").setAutoWidth(true);
        recentGrid.addColumn(r -> r.getSellerDetail() == null ? "" : r.getSellerDetail().getName())
                .setHeader("Seller").setAutoWidth(true);
        recentGrid.addColumn(r -> {
            if (r.getSeatReservations() == null) return "0";
            return String.valueOf(r.getSeatReservations().size());
        }).setHeader("# Seats").setAutoWidth(true);
        recentGrid.addColumn(ReservationDetail::getDescription).setHeader("Description").setFlexGrow(1);
        recentGrid.getElement().getThemeList().add("row-stripes");
        recentGrid.setWidthFull();
        recentGrid.setHeight("40vh");

        List<ReservationDetail> latest = reservationService.getAllReservations().stream()
                .sorted(Comparator.comparing(ReservationDetail::getId).reversed())
                .limit(10)
                .toList();
        recentGrid.setItems(latest);

        // Use the new content holder
        addToContent(statsRow, seatStatsRow, quickActions, recentGrid);
    }

    private Div statCard(VaadinIcon icon, String label, String value) {
        Icon i = icon.create();
        i.setSize("28px");
        i.getStyle().set("color", "var(--lumo-primary-color)");

        Span title = new Span(label);
        title.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Span number = new Span(value);
        number.getStyle().set("font-size", "24px")
                .set("font-weight", "700");

        Div content = new Div(title, number);
        content.getStyle().set("display", "flex").set("flex-direction", "column");

        HorizontalLayout row = new HorizontalLayout(i, content);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.setPadding(false);

        Div card = new Div(row);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "220px")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)");
        return card;
    }
}
