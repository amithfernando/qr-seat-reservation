package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.dto.ReportMetrics;
import com.qrseatreservation.core.report.ReportExportService;
import com.qrseatreservation.core.report.ReportExportService.Format;
import com.qrseatreservation.core.report.ReportExportService.ReportType;
import com.qrseatreservation.core.report.SellerReport;
import com.qrseatreservation.core.report.TicketReportRow;
import com.qrseatreservation.core.service.ReportService;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "reports", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Reports & Metrics")
@Slf4j
public class ReportView extends PageLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    private final ReportService reportService;
    private final ReportExportService exportService;

    private final VerticalLayout metricsLayout = new VerticalLayout();
    private final Anchor downloadAnchor = new Anchor();

    private List<TicketReportRow> rows = List.of();

    // Tickets sold by seller
    private final Div salesTotals = new Div();
    private final Div salesCards = new Div();
    // Seller check-ins
    private final Div checkInTotals = new Div();
    private final Div checkInCards = new Div();
    // Checked-in list
    private final TextField checkedFilter = new TextField();
    private final Span checkedCount = new Span();
    private final VirtualList<TicketReportRow> checkedList = new VirtualList<>();
    private final Span checkedEmpty = new Span();

    public ReportView(ReportService reportService, ReportExportService exportService) {
        super(VaadinIcon.BAR_CHART, "Reports & Metrics");
        this.reportService = reportService;
        this.exportService = exportService;

        Button refreshBtn = new Button("Refresh", new Icon(VaadinIcon.REFRESH), e -> refreshAll());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Div toolbar = new Div(refreshBtn);
        toolbar.addClassName("report-toolbar");

        // Hidden anchor used to start downloads once the file has been generated successfully
        downloadAnchor.getStyle().set("display", "none");

        metricsLayout.setPadding(false);
        metricsLayout.setSpacing(true);

        Div page = new Div(toolbar, metricsLayout, buildDownloads(), buildSalesSection(), buildCheckInSection(),
                buildCheckedListSection(), downloadAnchor);
        page.addClassName("report-page");
        addToContent(page);

        refreshAll();
    }

    private void refreshAll() {
        try {
            loadMetrics();
        } catch (Exception ex) {
            log.error("Failed to load metrics", ex);
            showError("Could not load metrics: " + ex.getMessage());
        }
        try {
            rows = exportService.ticketRows();
        } catch (Exception ex) {
            log.error("Failed to load report data", ex);
            showError("Could not load report data: " + ex.getMessage());
            rows = List.of();
        }
        renderSales();
        renderSellerCheckIns();
        applyCheckedFilter();
    }

    // ── Downloads ───────────────────────────────────────────────────────────

    private Component buildDownloads() {
        Div list = new Div();
        list.addClassName("report-exports");
        list.add(exportRow(ReportType.TICKET_LIST, "Every reserved ticket with seat, category, price and status"));
        list.add(exportRow(ReportType.SELLER_SALES, "Per-seller totals and every ticket they sold"));
        list.add(exportRow(ReportType.CHECK_INS, "All checked-in tickets, newest first"));
        list.add(exportRow(ReportType.SELLER_CHECK_INS, "Per-seller arrivals and checked-in tickets"));
        Details d = new Details("Downloads (Excel · CSV · PDF)", list);
        d.setOpened(true);
        d.addClassName("report-section");
        return d;
    }

    private Component exportRow(ReportType type, String description) {
        Span name = new Span(type.title);
        name.addClassName("name");
        Span desc = new Span(description);
        desc.addClassName("desc");
        Div text = new Div(name, desc);
        text.addClassName("text");

        Div buttons = new Div(
                exportButton(type, Format.XLSX, "Excel", VaadinIcon.FILE_TABLE),
                exportButton(type, Format.CSV, "CSV", VaadinIcon.FILE_TEXT_O),
                exportButton(type, Format.PDF, "PDF", VaadinIcon.FILE_O));
        buttons.addClassName("buttons");

        Div row = new Div(text, buttons);
        row.addClassName("report-export-row");
        return row;
    }

    private Button exportButton(ReportType type, Format format, String label, VaadinIcon icon) {
        Button b = new Button(label, icon.create());
        b.addThemeVariants(ButtonVariant.LUMO_SMALL);
        b.addClickListener(e -> download(type, format));
        b.setDisableOnClick(true);
        b.addClickListener(e -> b.setEnabled(true));
        return b;
    }

    /** Generates the file first so any error is shown, then triggers the browser download. */
    private void download(ReportType type, Format format) {
        try {
            byte[] bytes = exportService.export(type, format);
            String fileName = exportService.fileName(type, format);
            StreamResource res = new StreamResource(fileName, () -> new ByteArrayInputStream(bytes));
            res.setContentType(format.mimeType);
            downloadAnchor.setHref(res);
            downloadAnchor.getElement().setAttribute("download", fileName);
            downloadAnchor.getElement().callJsFunction("click");
        } catch (Exception ex) {
            log.error("Export {} as {} failed", type, format, ex);
            showError("Export failed: " + ex.getMessage());
        }
    }

    // ── Tickets sold by seller ──────────────────────────────────────────────

    private Component buildSalesSection() {
        salesTotals.addClassName("report-totals");
        salesCards.addClassName("report-cards");
        return section("Tickets Sold by Seller", "Revenue counts paid and checked-in tickets. Tap a seller to see ticket numbers.",
                salesTotals, salesCards);
    }

    private void renderSales() {
        List<SellerReport> sellers = ReportExportService.bySeller(rows).stream()
                .sorted(Comparator.comparingDouble(SellerReport::revenue).reversed()
                        .thenComparing(SellerReport::reserved, Comparator.reverseOrder()))
                .toList();
        long paid = sellers.stream().mapToLong(SellerReport::paid).sum();
        long unpaid = sellers.stream().mapToLong(SellerReport::unpaid).sum();
        double revenue = sellers.stream().mapToDouble(SellerReport::revenue).sum();
        salesTotals.removeAll();
        salesTotals.add(total("Sellers", String.valueOf(sellers.size())), total("Reserved", String.valueOf(rows.size())),
                total("Paid", String.valueOf(paid)), total("Unpaid", String.valueOf(unpaid)), total("Revenue", euro(revenue)));

        salesCards.removeAll();
        if (sellers.isEmpty()) salesCards.add(empty("No tickets sold yet."));
        for (SellerReport s : sellers) {
            Div head = cardHead(s.seller(), euro(s.revenue()));
            Div stats = new Div(pill(s.reserved() + " reserved", "neutral"), pill(s.paid() + " paid", "ok"),
                    pill(s.unpaid() + " unpaid", s.unpaid() > 0 ? "unpaid" : "neutral"), pill(s.checkedIn() + " in", "done"));
            stats.addClassName("report-pills");
            Div card = new Div(head, stats, ticketDetails(s.tickets(), "Ticket numbers"));
            card.addClassName("report-card");
            salesCards.add(card);
        }
    }

    // ── Seller check-ins ────────────────────────────────────────────────────

    private Component buildCheckInSection() {
        checkInTotals.addClassName("report-totals");
        checkInCards.addClassName("report-cards");
        return section("Seller Check-ins", "Arrivals out of paid tickets, per seller.", checkInTotals, checkInCards);
    }

    private void renderSellerCheckIns() {
        List<SellerReport> sellers = ReportExportService.bySeller(rows).stream()
                .filter(s -> s.paid() > 0)
                .sorted(Comparator.comparingLong(SellerReport::checkedIn).reversed()
                        .thenComparing(SellerReport::paid, Comparator.reverseOrder()))
                .toList();
        long paid = sellers.stream().mapToLong(SellerReport::paid).sum();
        long in = sellers.stream().mapToLong(SellerReport::checkedIn).sum();
        checkInTotals.removeAll();
        checkInTotals.add(total("Checked in", String.valueOf(in)), total("Not arrived", String.valueOf(paid - in)),
                total("Arrival rate", paid > 0 ? String.format("%.0f%%", in * 100.0 / paid) : "–"));

        checkInCards.removeAll();
        if (sellers.isEmpty()) checkInCards.add(empty("No paid tickets yet."));
        for (SellerReport s : sellers) {
            Div head = cardHead(s.seller(), s.checkedIn() + " / " + s.paid());
            ProgressBar bar = new ProgressBar(0, 1, s.paid() > 0 ? (double) s.checkedIn() / s.paid() : 0);
            if (s.checkedIn() == s.paid()) bar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
            Span sub = new Span(s.notArrived() == 0 ? "Everyone has arrived" : s.notArrived() + " not arrived yet");
            sub.addClassName("report-sub");
            List<TicketReportRow> paidTickets = s.tickets().stream().filter(TicketReportRow::isPaid)
                    .sorted(Comparator.comparing(TicketReportRow::isCheckedIn).reversed()
                            .thenComparing(TicketReportRow::ticketNo)).toList();
            Div card = new Div(head, bar, sub, ticketDetails(paidTickets, "Tickets"));
            card.addClassName("report-card");
            checkInCards.add(card);
        }
    }

    // ── Checked-in list ─────────────────────────────────────────────────────

    private Component buildCheckedListSection() {
        checkedFilter.setPlaceholder("Search ticket, table, seat, seller, buyer…");
        checkedFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        checkedFilter.setClearButtonVisible(true);
        checkedFilter.setValueChangeMode(ValueChangeMode.LAZY);
        checkedFilter.addValueChangeListener(e -> applyCheckedFilter());
        checkedFilter.addClassName("grow");
        checkedCount.addClassName("report-sub");
        Div bar = new Div(checkedFilter, checkedCount);
        bar.addClassName("report-filter-bar");

        checkedList.setRenderer(new ComponentRenderer<>(this::checkedRow));
        checkedList.addClassName("report-list");
        checkedEmpty.addClassName("checkin-empty");
        return section("Checked-In Tickets", "Newest first.", bar, checkedEmpty, checkedList);
    }

    private void applyCheckedFilter() {
        String q = checkedFilter.getValue() == null ? "" : checkedFilter.getValue().trim().toLowerCase(Locale.ROOT);
        List<TicketReportRow> all = ReportExportService.checkedIn(rows);
        List<TicketReportRow> shown = q.isEmpty() ? all : all.stream().filter(r -> matches(r, q)).toList();
        checkedList.setItems(shown);
        checkedCount.setText(shown.size() + (q.isEmpty() ? "" : " of " + all.size()) + " checked in");
        checkedList.setVisible(!shown.isEmpty());
        checkedEmpty.setVisible(shown.isEmpty());
        checkedEmpty.setText(all.isEmpty() ? "Nobody has checked in yet." : "No checked-in tickets match your search.");
    }

    private Component checkedRow(TicketReportRow r) {
        Span ticket = new Span(r.ticketNo());
        ticket.addClassName("ticket");
        Span sub = new Span(String.join(" · ", List.of(
                "Table " + (r.table().isBlank() ? "–" : r.table()),
                "Seat " + (r.seat().isBlank() ? "–" : r.seat()),
                r.seller())));
        sub.addClassName("sub");
        Div main = new Div(ticket, sub);
        main.addClassName("main");
        Span time = new Span(r.checkedInAt() != null ? r.checkedInAt().format(TIME) : "");
        time.addClassName("time");
        Div row = new Div(main, time);
        row.addClassName("checkin-list-row");
        return row;
    }

    private static boolean matches(TicketReportRow r, String q) {
        for (String s : new String[]{r.ticketNo(), r.table(), r.seat(), r.seller(), r.buyerName(), r.category()}) {
            if (s != null && s.toLowerCase(Locale.ROOT).contains(q)) return true;
        }
        return false;
    }

    // ── Shared UI helpers ───────────────────────────────────────────────────

    private static Component section(String title, String subtitle, Component... content) {
        H3 h = new H3(title);
        Span sub = new Span(subtitle);
        sub.addClassName("report-sub");
        Div head = new Div(h, sub);
        head.addClassName("report-section-head");
        Div s = new Div(head);
        s.add(content);
        s.addClassName("report-section");
        return s;
    }

    private static Div cardHead(String name, String value) {
        Span n = new Span(name);
        n.addClassName("name");
        Span v = new Span(value);
        v.addClassName("value");
        Div d = new Div(n, v);
        d.addClassName("report-card-head");
        return d;
    }

    private static Details ticketDetails(List<TicketReportRow> tickets, String label) {
        Div chips = new Div();
        chips.addClassName("report-chips");
        for (TicketReportRow t : tickets) {
            Span chip = pill(t.ticketNo(), t.isCheckedIn() ? "done" : t.isPaid() ? "ok" : "unpaid");
            chip.getElement().setAttribute("title", "Table " + t.table() + " · Seat " + t.seat() + " · " + t.statusLabel());
            chips.add(chip);
        }
        Span legend = new Span("Green = paid · Grey = checked in · Orange = unpaid");
        legend.addClassName("report-sub");
        Details d = new Details(label + " (" + tickets.size() + ")", chips, legend);
        d.addClassName("report-details");
        return d;
    }

    private static Span pill(String text, String kind) {
        Span p = new Span(text);
        p.addClassNames("pill", kind);
        return p;
    }

    private static Div total(String label, String value) {
        Span v = new Span(value);
        v.addClassName("value");
        Span l = new Span(label);
        l.addClassName("label");
        Div d = new Div(v, l);
        d.addClassName("report-total");
        return d;
    }

    private static Span empty(String text) {
        Span s = new Span(text);
        s.addClassName("checkin-empty");
        return s;
    }

    private static String euro(double v) {
        return "€" + String.format("%,.2f", v);
    }

    private static HorizontalLayout metricRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void loadMetrics() {
        metricsLayout.removeAll();
        
        ReportMetrics metrics = reportService.getReportMetrics();
        
        // Financial Metrics Section
        H3 financialHeader = new H3("Financial Metrics");
        financialHeader.addClassName("section-title");
        financialHeader.getStyle().set("margin-top", "20px");
        metricsLayout.add(financialHeader);
        
        HorizontalLayout financialMetrics = metricRow();
        financialMetrics.setWidthFull();
        financialMetrics.add(
                createMetricCard(VaadinIcon.MONEY, "Total Revenue (Paid)",
                        "€" + String.format("%.2f", metrics.getTotalRevenue() != null ? metrics.getTotalRevenue() : 0.0), "#4CAF50"),
                createMetricCard(VaadinIcon.TICKET, "Tickets Sold (Paid)",
                        String.valueOf(metrics.getTotalTicketsSold() != null ? metrics.getTotalTicketsSold() : 0), "#2196F3"),
                createMetricCard(VaadinIcon.GIFT, "Complimentary (INVITATION)",
                        String.valueOf(metrics.getComplimentaryCheckedIn()), "#9C27B0"),
                createMetricCard(VaadinIcon.CHART, "Avg Ticket Price",
                        "€" + String.format("%.2f", metrics.getAverageTicketPrice() != null ? metrics.getAverageTicketPrice() : 0.0), "#FF9800")
        );
        metricsLayout.add(financialMetrics);

        long totalPaidTickets = metrics.getTotalTicketsSold() != null ? metrics.getTotalTicketsSold() : 0;
        long totalCheckedInTickets = totalPaidTickets + metrics.getComplimentaryCheckedIn();
        double checkInRate = metrics.getOccupiedSeats() > 0
                ? totalCheckedInTickets * 100.0 / metrics.getOccupiedSeats()
                : 0.0;

        H3 analyticsHeader = new H3("Analytical Dashboard");
        analyticsHeader.addClassName("section-title");
        analyticsHeader.getStyle().set("margin-top", "20px");
        metricsLayout.add(analyticsHeader);

        HorizontalLayout analyticsMetrics = metricRow();
        analyticsMetrics.setWidthFull();
        analyticsMetrics.add(
                createMetricCard(VaadinIcon.CHECK_SQUARE, "Checked-in Tickets",
                        String.valueOf(totalCheckedInTickets), "#00695C"),
                createMetricCard(VaadinIcon.LINE_CHART, "Check-in Rate",
                        String.format("%.1f%%", checkInRate), "#673AB7"),
                createMetricCard(VaadinIcon.CIRCLE_THIN, "Available Seats",
                        String.valueOf(metrics.getAvailableSeats()), "#2E7D32"),
                createMetricCard(VaadinIcon.CALC_BOOK, "Revenue / Paid Ticket",
                        "€" + String.format("%.2f", metrics.getAverageTicketPrice() != null ? metrics.getAverageTicketPrice() : 0.0), "#795548")
        );
        metricsLayout.add(analyticsMetrics);
        
        // Revenue by Category Section (if data available)
        if (metrics.getRevenueByCategory() != null && !metrics.getRevenueByCategory().isEmpty()) {
            H4 categoryHeader = new H4("Revenue by Category");
            categoryHeader.addClassName("section-title");
            categoryHeader.getStyle().set("margin-top", "20px");
            metricsLayout.add(categoryHeader);
            
            Grid<CategorySummary> categoryGrid = new Grid<>(CategorySummary.class, false);
            categoryGrid.addColumn(CategorySummary::getCategoryName).setHeader("Category").setAutoWidth(true);
            categoryGrid.addColumn(CategorySummary::getTicketCount).setHeader("Tickets").setAutoWidth(true);
            categoryGrid.addColumn(cs -> "€" + String.format("%.2f", cs.getRevenue())).setHeader("Revenue").setAutoWidth(true);
            categoryGrid.addColumn(cs -> String.format("%.1f%%", cs.getPercentage())).setHeader("% of Total").setAutoWidth(true);
            
            double totalRev = metrics.getTotalRevenue() != null ? metrics.getTotalRevenue() : 0.0;
            java.util.List<CategorySummary> categorySummaries = new java.util.ArrayList<>();
            
            metrics.getRevenueByCategory().forEach((category, revenue) -> {
                long count = metrics.getTicketCountByCategory().getOrDefault(category, 0L);
                double percentage = totalRev > 0 ? (revenue / totalRev) * 100 : 0;
                categorySummaries.add(new CategorySummary(category, count, revenue, percentage));
            });
            
            // Sort by revenue descending
            categorySummaries.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
            
            categoryGrid.setItems(categorySummaries);
            categoryGrid.setAllRowsVisible(true);
            metricsLayout.add(categoryGrid);
        }
        
        // Seat Metrics Section
        H3 seatHeader = new H3("Seat Metrics");
        seatHeader.addClassName("section-title");
        seatHeader.getStyle().set("margin-top", "20px");
        metricsLayout.add(seatHeader);
        
        HorizontalLayout seatMetrics = metricRow();
        seatMetrics.setWidthFull();
        seatMetrics.add(
                createMetricCard(VaadinIcon.GRID, "Total Seats", 
                        String.valueOf(metrics.getTotalSeats()), "#1976D2"),
                createMetricCard(VaadinIcon.CIRCLE_THIN, "Occupied", 
                        String.valueOf(metrics.getOccupiedSeats()), "#F44336"),
                createMetricCard(VaadinIcon.BAN, "Unavailable", 
                        String.valueOf(metrics.getUnavailableSeats()), "#9E9E9E"),
                createMetricCard(VaadinIcon.CHART, "Occupancy", 
                        String.format("%.1f%%", metrics.getSeatOccupancyPercentage()), "#FF5722")
        );
        metricsLayout.add(seatMetrics);
        
        // Reservation Metrics Section
        H3 reservationHeader = new H3("Reservation Metrics");
        reservationHeader.addClassName("section-title");
        reservationHeader.getStyle().set("margin-top", "20px");
        metricsLayout.add(reservationHeader);
        
        HorizontalLayout reservationMetrics = metricRow();
        reservationMetrics.setWidthFull();
        reservationMetrics.add(
                createMetricCard(VaadinIcon.CHECK_CIRCLE, "Paid Reservations", 
                        String.valueOf(metrics.getPaidReservations()), "#4CAF50")
        );
        metricsLayout.add(reservationMetrics);
    }
    
    // Inner class for category summary
    public static class CategorySummary {
        private final String categoryName;
        private final long ticketCount;
        private final double revenue;
        private final double percentage;
        
        public CategorySummary(String categoryName, long ticketCount, double revenue, double percentage) {
            this.categoryName = categoryName;
            this.ticketCount = ticketCount;
            this.revenue = revenue;
            this.percentage = percentage;
        }
        
        public String getCategoryName() { return categoryName; }
        public long getTicketCount() { return ticketCount; }
        public double getRevenue() { return revenue; }
        public double getPercentage() { return percentage; }
    }
    
    private Div createMetricCard(VaadinIcon iconType, String title, String value, String color) {
        Div card = new Div();
        card.addClassName("metric-card");
        card.getStyle()
                .set("padding", "16px")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("border-left", "4px solid " + color)
                .set("flex", "1 1 160px")
                .set("min-width", "150px")
                .set("box-sizing", "border-box");
        
        Icon icon = new Icon(iconType);
        icon.addClassName("metric-icon");
        icon.setSize("32px");
        icon.setColor(color);
        
        Span titleSpan = new Span(title);
        titleSpan.addClassName("metric-title");
        titleSpan.getStyle()
                .set("display", "block")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px")
                .set("margin-top", "10px");
        
        Span valueSpan = new Span(value);
        valueSpan.addClassName("metric-value");
        valueSpan.getStyle()
                .set("display", "block")
                .set("color", "var(--lumo-body-text-color)")
                .set("font-size", "28px")
                .set("font-weight", "bold")
                .set("margin-top", "5px");
        
        VerticalLayout content = new VerticalLayout(icon, titleSpan, valueSpan);
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(FlexComponent.Alignment.START);
        
        card.add(content);
        return card;
    }
    
}
