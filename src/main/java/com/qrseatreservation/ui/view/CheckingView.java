package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.service.ReservationService;
import com.qrseatreservation.core.service.TicketService;
import com.qrseatreservation.ui.view.component.QrScanner;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Gate check-in screen, designed mobile-first: live scanner, big status card, tap-friendly lists. */
@Route(value = "checkin", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "ENTRANCE"})
@PageTitle("Check In")
@Slf4j
public class CheckingView extends PageLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private record Row(SeatReservation sr, ReservationDetail rd) {
        String ticketNo() { return sr.getTicketNo(); }
        String table() {
            return sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                    ? sr.getSeatDetail().getTableDetail().getTableName() : "";
        }
        String seat() { return sr.getSeatDetail() != null && sr.getSeatDetail().getSeatNo() != null ? sr.getSeatDetail().getSeatNo() : ""; }
        String seller() { return rd.getSellerDetail() != null && rd.getSellerDetail().getName() != null ? rd.getSellerDetail().getName() : ""; }
        ReservationStatus status() { return sr.getReservationStatus(); }
    }

    private final ReservationService reservationService;
    private final TicketService ticketService;
    private final boolean isAdmin;

    private List<Row> rows = List.of();

    // Stats
    private final Span checkedInNo = new Span("-");
    private final Span waitingNo = new Span("-");
    private final Span unpaidNo = new Span("-");
    private final Span sessionBadge = new Span();
    private int sessionCount;

    // Scanner
    private final Div scannerHolder = new Div();
    private final QrScanner scanner = new QrScanner();
    private final Div scannerOverlay = new Div();
    private final Button cameraBtn = new Button();
    private final Button switchBtn = new Button(VaadinIcon.REFRESH.create());
    private String lastScanValue;
    private long lastScanAt;
    /** Tickets this door admitted in the last few seconds; the guest is usually still holding the phone up. */
    private final java.util.Map<String, Long> recentlyAdmitted = new java.util.HashMap<>();
    private static final long READMIT_IGNORE_MS = 8000;

    // Manual entry + search
    private final TextField ticketField = new TextField();
    private final ComboBox<String> tableCombo = new ComboBox<>("Table");
    private final ComboBox<String> sellerCombo = new ComboBox<>("Seller");
    private final Div searchResults = new Div();
    private final Details searchDetails = new Details();

    // Recent check-ins
    private final TextField recentFilter = new TextField();
    private final VirtualList<Row> recentList = new VirtualList<>();
    private final Span recentEmpty = new Span("No check-ins yet.");

    // Result sheet
    private final Dialog sheet = new Dialog();

    public CheckingView(ReservationService reservationService, TicketService ticketService) {
        super(VaadinIcon.QRCODE, "Check In");
        this.reservationService = reservationService;
        this.ticketService = ticketService;
        this.isAdmin = hasRole("ADMIN");
        setSizeFull();

        Div page = new Div(buildStats(), buildScanCard(), buildRecentCard());
        page.addClassName("checkin-page");
        addToContent(page);

        sheet.addClassName("checkin-sheet");
        sheet.setModal(true);
        sheet.setCloseOnOutsideClick(true);
        sheet.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                lastScanValue = null;
                scanner.clearLastScan();
            }
        });

        updateSessionBadge();
        loadData();
        setCameraOn(true);
    }

    // ── Layout ──────────────────────────────────────────────────────────────

    private Component buildStats() {
        Div grid = new Div(
                stat("Checked in", checkedInNo, "#16a34a"),
                stat("Paid, not arrived", waitingNo, "#2563eb"),
                stat("Unpaid", unpaidNo, "#ea580c"));
        grid.addClassName("checkin-stats");
        return grid;
    }

    private Div stat(String label, Span value, String color) {
        value.addClassName("value");
        Span l = new Span(label);
        l.addClassName("label");
        Div d = new Div(value, l);
        d.addClassName("checkin-stat");
        d.getStyle().set("--stat-color", color);
        return d;
    }

    private Component buildScanCard() {
        H4 title = new H4("Scan ticket");
        sessionBadge.addClassNames("pill", "ok");
        cameraBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        cameraBtn.addClickListener(e -> setCameraOn(!scanner.isActive()));
        switchBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        switchBtn.setText("Flip");
        switchBtn.setTooltipText("Switch between back and front camera");
        switchBtn.addClickListener(e -> { scanner.switchCamera(); showScannerMessage("Switching camera…", null); });
        Div head = new Div(title, sessionBadge, switchBtn, cameraBtn);
        head.addClassName("checkin-card-head");

        scanner.setSizeFull();
        scanner.addScanListener(this::onScan);
        scanner.addStartedListener(() -> { scannerOverlay.setVisible(false); scannerHolder.removeClassName("off"); });
        scanner.addErrorListener(this::onCameraError);
        scannerOverlay.addClassName("checkin-scanner-msg");
        scannerHolder.addClassName("checkin-scanner");
        scannerHolder.add(scanner, scannerOverlay);

        ticketField.setPlaceholder("Or type ticket no. e.g. DJ0001");
        ticketField.setPrefixComponent(VaadinIcon.TICKET.create());
        ticketField.setClearButtonVisible(true);
        ticketField.setAutocapitalize(Autocapitalize.CHARACTERS);
        ticketField.setAutocorrect(false);
        ticketField.addKeyPressListener(Key.ENTER, e -> lookupTyped());
        Button findBtn = new Button("Find", e -> lookupTyped());
        findBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Div manual = new Div(ticketField, findBtn);
        manual.addClassName("checkin-manual");

        tableCombo.setClearButtonVisible(true);
        sellerCombo.setClearButtonVisible(true);
        tableCombo.addValueChangeListener(e -> runSearch());
        sellerCombo.addValueChangeListener(e -> runSearch());
        Div filters = new Div(tableCombo, sellerCombo);
        filters.addClassName("checkin-filters");
        searchResults.addClassName("checkin-list");
        searchDetails.setSummaryText("Find by table or seller");
        searchDetails.add(filters, searchResults);
        searchDetails.setWidthFull();

        Div card = new Div(head, scannerHolder, manual, searchDetails);
        card.addClassName("checkin-card");
        return card;
    }

    private Component buildRecentCard() {
        H4 title = new H4("Recent check-ins");
        Button refresh = new Button(VaadinIcon.REFRESH.create(), e -> loadData());
        refresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        refresh.setAriaLabel("Refresh");
        Div head = new Div(title, refresh);
        head.addClassName("checkin-card-head");

        recentFilter.setPlaceholder("Filter by ticket, table, seat, seller…");
        recentFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        recentFilter.setClearButtonVisible(true);
        recentFilter.setValueChangeMode(ValueChangeMode.LAZY);
        recentFilter.setWidthFull();
        recentFilter.addValueChangeListener(e -> applyRecentFilter());

        recentList.setRenderer(new ComponentRenderer<>(this::listRow));
        recentList.addClassName("checkin-recent");
        recentEmpty.addClassName("checkin-empty");

        Div card = new Div(head, recentFilter, recentEmpty, recentList);
        card.addClassName("checkin-card");
        return card;
    }

    private Component listRow(Row r) {
        Span ticket = new Span(r.ticketNo());
        ticket.addClassName("ticket");
        Span sub = new Span(joinNonBlank(" · ", "Table " + r.table(), "Seat " + r.seat(), r.seller()));
        sub.addClassName("sub");
        Div main = new Div(ticket, sub);
        main.addClassName("main");

        Span right;
        if (r.status() == ReservationStatus.CHECKED_IN) {
            right = new Span(r.sr().getUpdatedAt() != null ? r.sr().getUpdatedAt().format(TIME) : "");
            right.addClassName("time");
        } else {
            right = statusPill(r.status());
        }
        Icon chevron = VaadinIcon.ANGLE_RIGHT.create();
        chevron.addClassName("chevron");

        Div row = new Div(main, right, chevron);
        row.addClassNames("checkin-list-row", "clickable");
        row.addClickListener(e -> openTicket(r.ticketNo()));
        return row;
    }

    // ── Data ────────────────────────────────────────────────────────────────

    private void loadData() {
        try {
            List<Row> all = new ArrayList<>();
            for (ReservationDetail rd : reservationService.getAllReservations()) {
                if (rd.getSeatReservations() == null) continue;
                for (SeatReservation sr : rd.getSeatReservations()) {
                    if (sr != null && sr.getTicketNo() != null) all.add(new Row(sr, rd));
                }
            }
            rows = all;

            checkedInNo.setText(String.valueOf(count(ReservationStatus.CHECKED_IN)));
            waitingNo.setText(String.valueOf(count(ReservationStatus.PAID)));
            unpaidNo.setText(String.valueOf(count(ReservationStatus.PAYMENT_PENDING)));

            tableCombo.setItems(rows.stream().map(Row::table).filter(s -> !s.isBlank()).distinct().sorted().toList());
            sellerCombo.setItems(rows.stream().map(Row::seller).filter(s -> !s.isBlank()).distinct().sorted().toList());
            applyRecentFilter();
            if (tableCombo.getValue() != null || sellerCombo.getValue() != null) runSearch();
        } catch (Exception ex) {
            log.error("Failed to load check-in data", ex);
            notifyError("Could not load data: " + ex.getMessage());
        }
    }

    private long count(ReservationStatus st) {
        return rows.stream().filter(r -> r.status() == st).count();
    }

    private void applyRecentFilter() {
        String q = recentFilter.getValue() == null ? "" : recentFilter.getValue().trim().toLowerCase(Locale.ROOT);
        List<Row> list = rows.stream()
                .filter(r -> r.status() == ReservationStatus.CHECKED_IN)
                .filter(r -> q.isEmpty() || matches(r, q))
                .sorted(Comparator.comparing((Row r) -> r.sr().getUpdatedAt(),
                        Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder())).reversed())
                .limit(200)
                .toList();
        recentList.setItems(list);
        recentList.setVisible(!list.isEmpty());
        recentEmpty.setVisible(list.isEmpty());
        recentEmpty.setText(q.isEmpty() ? "No check-ins yet." : "No check-ins match \"" + q + "\".");
    }

    private static boolean matches(Row r, String q) {
        return contains(r.ticketNo(), q) || contains(r.table(), q) || contains(r.seat(), q)
                || contains(r.seller(), q) || contains(r.sr().getBuyerName(), q);
    }

    private static boolean contains(String s, String q) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(q);
    }

    private void runSearch() {
        String table = tableCombo.getValue();
        String seller = sellerCombo.getValue();
        searchResults.removeAll();
        if (table == null && seller == null) return;
        List<Row> found = rows.stream()
                .filter(r -> table == null || table.equals(r.table()))
                .filter(r -> seller == null || seller.equals(r.seller()))
                .sorted(Comparator.comparing(Row::table).thenComparing(Row::seat))
                .limit(150)
                .toList();
        showResults(found, found.isEmpty() ? "No tickets found." : found.size() + " ticket(s) — tap to open");
    }

    private void showResults(List<Row> found, String caption) {
        searchResults.removeAll();
        Span c = new Span(caption);
        c.addClassName("checkin-empty");
        searchResults.add(c);
        found.forEach(r -> searchResults.add(listRow(r)));
        searchDetails.setOpened(true);
    }

    // ── Scanner ─────────────────────────────────────────────────────────────

    private void setCameraOn(boolean on) {
        scanner.setActive(on);
        cameraBtn.setText(on ? "Stop camera" : "Start camera");
        cameraBtn.setIcon(on ? VaadinIcon.STOP.create() : VaadinIcon.CAMERA.create());
        switchBtn.setVisible(on);
        if (on) showScannerMessage("Starting camera…", null);
        else showScannerMessage("Camera off.", "Start camera");
    }

    private void onCameraError(String name, String message) {
        log.warn("Camera could not start: {} - {}", name, message);
        String msg = switch (name == null ? "" : name) {
            case "NotAllowedError", "SecurityError" -> "Camera access is blocked. Allow the camera for this site in the browser settings, then tap Try again.";
            case "NotFoundError", "OverconstrainedError" -> "No camera found on this device.";
            case "NotReadableError", "AbortError" -> "The camera is busy (another app or tab is using it). Close it and tap Try again.";
            case "NotSupportedError" -> message;
            default -> "Camera could not start" + (message != null && !message.isBlank() ? ": " + message : ".");
        };
        scanner.setActive(false);
        cameraBtn.setText("Start camera");
        cameraBtn.setIcon(VaadinIcon.CAMERA.create());
        switchBtn.setVisible(false);
        showScannerMessage(msg + " You can still type the ticket number below.", "Try again");
    }

    private void showScannerMessage(String text, String buttonLabel) {
        scannerOverlay.removeAll();
        Icon cam = VaadinIcon.CAMERA.create();
        cam.setSize("36px");
        scannerOverlay.add(cam, new Span(text));
        if (buttonLabel != null) {
            Button retry = new Button(buttonLabel, VaadinIcon.CAMERA.create(), e -> setCameraOn(true));
            retry.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            scannerOverlay.add(retry);
        }
        scannerOverlay.setVisible(true);
        scannerHolder.addClassName("off");
    }

    private void onScan(String value) {
        if (value == null || value.isBlank()) return;
        String v = value.trim();
        long now = System.currentTimeMillis();
        recentlyAdmitted.values().removeIf(t -> now - t > READMIT_IGNORE_MS);
        // Camera still sees the ticket that was just checked in at this door: ignore instead of showing "already checked in"
        if (recentlyAdmitted.containsKey(v)) return;
        // The camera reports the same code many times per second while it's in view
        if (v.equals(lastScanValue) && now - lastScanAt < 4000) return;
        lastScanValue = v;
        lastScanAt = now;
        openTicket(v);
    }

    private void lookupTyped() {
        String v = ticketField.getValue();
        if (v == null || v.isBlank()) {
            ticketField.focus();
            return;
        }
        openTicket(v.trim());
        ticketField.clear();
    }

    // ── Result sheet ────────────────────────────────────────────────────────

    private void openTicket(String ticketNo) {
        loadData(); // always show the latest status (other gates may have checked the ticket in)
        Row exact = rows.stream().filter(r -> ticketNo.equals(r.ticketNo())).findFirst()
                .or(() -> rows.stream().filter(r -> ticketNo.equalsIgnoreCase(r.ticketNo())).findFirst())
                .orElse(null);
        if (exact != null) { showSheet(exact); return; }

        String q = ticketNo.toLowerCase(Locale.ROOT);
        List<Row> partial = rows.stream().filter(r -> contains(r.ticketNo(), q)).limit(50).toList();
        if (partial.size() == 1) { showSheet(partial.get(0)); return; }
        if (partial.size() > 1) {
            showResults(partial, partial.size() + " tickets contain \"" + ticketNo + "\" — tap one");
            return;
        }
        showNotFound(ticketNo);
    }

    private void showSheet(Row row) {
        SeatReservation sr = row.sr();
        ReservationStatus st = sr.getReservationStatus();
        sheet.removeAll();
        sheet.getFooter().removeAll();

        Component banner = switch (st == null ? ReservationStatus.PAYMENT_PENDING : st) {
            case PAID -> banner("state-ok", VaadinIcon.CHECK_CIRCLE, "VALID — PAID", "Ready to check in");
            case CHECKED_IN -> banner("state-done", VaadinIcon.USER_CHECK, "ALREADY CHECKED IN",
                    sr.getUpdatedAt() != null ? "Entered at " + sr.getUpdatedAt().format(TIME) : "This ticket was already used");
            case PAYMENT_PENDING -> banner("state-unpaid", VaadinIcon.WARNING, "NOT PAID",
                    isAdmin ? "Collect payment before entry" : "Send the guest to the organiser to pay");
        };

        H2 ticket = new H2(sr.getTicketNo());
        ticket.addClassName("checkin-ticketno");

        Div tiles = new Div(tile("Table", row.table()), tile("Seat", row.seat()));
        tiles.addClassName("checkin-tiles");

        Div meta = new Div();
        meta.addClassName("checkin-meta");
        addMeta(meta, "Type", sr.getTicketType() != null ? sr.getTicketType().name() : null);
        addMeta(meta, "Seller", row.seller());
        addMeta(meta, "Buyer", sr.getBuyerName());

        sheet.add(banner, ticket, tiles, meta);
        Component group = groupSection(row);
        if (group != null) sheet.add(group);

        Div actions = new Div();
        actions.addClassName("checkin-actions");
        if (st == ReservationStatus.PAID) {
            Button checkIn = bigButton("CHECK IN", VaadinIcon.SIGN_IN, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            checkIn.addClickListener(e -> checkIn(row));
            actions.add(checkIn);
        } else if (st == ReservationStatus.PAYMENT_PENDING && isAdmin) {
            Button payAndIn = bigButton("Mark paid & check in", VaadinIcon.CASH, ButtonVariant.LUMO_PRIMARY);
            payAndIn.addClickListener(e -> payAndCheckIn(row));
            actions.add(payAndIn);
        } else if (st == ReservationStatus.CHECKED_IN) {
            Button undo = new Button("Undo check-in", VaadinIcon.ARROW_BACKWARD.create());
            undo.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            undo.addClickListener(e -> confirmUndo(row));
            actions.add(undo);
        }
        Button next = bigButton("Next ticket", VaadinIcon.QRCODE, ButtonVariant.LUMO_CONTRAST);
        next.addClickListener(e -> sheet.close());
        actions.add(next);
        sheet.getFooter().add(actions);
        sheet.open();
    }

    private Component groupSection(Row row) {
        ReservationDetail rd = row.rd();
        if (rd.getSeatReservations() == null || rd.getSeatReservations().size() < 2) return null;
        List<SeatReservation> group = rd.getSeatReservations().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(s -> s.getTicketNo() == null ? "" : s.getTicketNo()))
                .toList();
        long in = group.stream().filter(s -> s.getReservationStatus() == ReservationStatus.CHECKED_IN).count();
        long paid = group.stream().filter(s -> s.getReservationStatus() == ReservationStatus.PAID).count();
        long unpaid = group.size() - in - paid;

        Span head = new Span("Same booking: " + group.size() + " tickets · " + in + " in · " + paid + " paid"
                + (unpaid > 0 ? " · " + unpaid + " unpaid" : ""));
        head.addClassName("checkin-group-head");
        Div chips = new Div();
        chips.addClassName("chips");
        for (SeatReservation s : group) {
            Span chip = statusPill(s.getReservationStatus());
            chip.setText(s.getTicketNo() + (s.getSeatDetail() != null && s.getSeatDetail().getSeatNo() != null ? " · " + s.getSeatDetail().getSeatNo() : ""));
            if (s.getTicketNo() != null && s.getTicketNo().equals(row.ticketNo())) chip.addClassName("current");
            chips.add(chip);
        }
        Div box = new Div(head, chips);
        box.addClassName("checkin-group");
        if (paid > 1) {
            Button all = new Button("Check in all " + paid + " paid tickets", VaadinIcon.USERS.create());
            all.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            all.setWidthFull();
            all.addClickListener(e -> checkInGroup(rd, paid));
            box.add(all);
        }
        return box;
    }

    private void showNotFound(String ticketNo) {
        sheet.removeAll();
        sheet.getFooter().removeAll();
        boolean exists = ticketService.exists(ticketNo) || ticketService.exists(ticketNo.toUpperCase(Locale.ROOT));
        boolean otherEvent = !exists && (ticketService.existsInOtherEvent(ticketNo)
                || ticketService.existsInOtherEvent(ticketNo.toUpperCase(Locale.ROOT)));
        sheet.add(banner("state-missing", VaadinIcon.CLOSE_CIRCLE, "NOT VALID",
                exists ? "This ticket was never sold / reserved"
                        : otherEvent ? "This ticket belongs to a different event" : "No ticket with this number"));
        H2 t = new H2(ticketNo);
        t.addClassName("checkin-ticketno");
        sheet.add(t);
        Div actions = new Div(bigButton("Next ticket", VaadinIcon.QRCODE, ButtonVariant.LUMO_CONTRAST));
        actions.addClassName("checkin-actions");
        ((Button) actions.getComponentAt(0)).addClickListener(e -> sheet.close());
        sheet.getFooter().add(actions);
        vibrate(300);
        sheet.open();
    }

    // ── Actions ─────────────────────────────────────────────────────────────

    private void checkIn(Row row) {
        try {
            if (reservationService.checkInByTicketNo(row.ticketNo())) {
                sessionCount++;
                recentlyAdmitted.put(row.ticketNo(), System.currentTimeMillis());
                afterCheckIn(row.ticketNo() + " checked in — Table " + row.table() + " · Seat " + row.seat());
            } else {
                notifyError("Could not check in — status changed. Showing latest.");
                openTicket(row.ticketNo());
            }
        } catch (Exception ex) {
            log.error("Check-in failed for {}", row.ticketNo(), ex);
            notifyError("Check-in failed: " + ex.getMessage());
        }
    }

    private void payAndCheckIn(Row row) {
        try {
            reservationService.setTicketPaid(row.sr().getId(), true);
            checkIn(row);
        } catch (Exception ex) {
            log.error("Pay & check-in failed for {}", row.ticketNo(), ex);
            notifyError("Failed: " + ex.getMessage());
        }
    }

    private void checkInGroup(ReservationDetail rd, long expected) {
        try {
            int n = reservationService.checkInAllPaid(rd.getId());
            sessionCount += n;
            long now = System.currentTimeMillis();
            if (rd.getSeatReservations() != null) {
                rd.getSeatReservations().forEach(sr -> { if (sr.getTicketNo() != null) recentlyAdmitted.put(sr.getTicketNo(), now); });
            }
            afterCheckIn(n + " ticket(s) checked in" + (n != expected ? " (others changed meanwhile)" : ""));
        } catch (Exception ex) {
            log.error("Group check-in failed", ex);
            notifyError("Group check-in failed: " + ex.getMessage());
        }
    }

    private void afterCheckIn(String message) {
        sheet.close();
        updateSessionBadge();
        loadData();
        vibrate(120);
        Notification n = Notification.show("✓ " + message, 2500, Notification.Position.TOP_STRETCH);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void confirmUndo(Row row) {
        ConfirmDialog dlg = new ConfirmDialog("Undo check-in?",
                "Ticket " + row.ticketNo() + " will be marked as not arrived (PAID) again.", "Undo", e -> {
            try {
                if (reservationService.undoCheckIn(row.ticketNo())) {
                    if (sessionCount > 0) sessionCount--;
                    updateSessionBadge();
                    Notification.show("Check-in undone for " + row.ticketNo(), 2500, Notification.Position.TOP_STRETCH);
                }
                openTicket(row.ticketNo());
            } catch (Exception ex) {
                notifyError("Undo failed: " + ex.getMessage());
            }
        });
        dlg.setConfirmButtonTheme("error primary");
        dlg.setCancelable(true);
        dlg.open();
    }

    // ── Small helpers ───────────────────────────────────────────────────────

    private Div banner(String stateClass, VaadinIcon icon, String title, String subtitle) {
        Icon i = icon.create();
        i.setSize("40px");
        Span t = new Span(title);
        t.addClassName("title");
        Span s = new Span(subtitle);
        s.addClassName("subtitle");
        Div text = new Div(t, s);
        Div b = new Div(i, text);
        b.addClassNames("checkin-banner", stateClass);
        return b;
    }

    private static Div tile(String label, String value) {
        Span k = new Span(label);
        k.addClassName("k");
        Span v = new Span(value == null || value.isBlank() ? "–" : value);
        v.addClassName("v");
        Div d = new Div(k, v);
        d.addClassName("checkin-tile");
        return d;
    }

    private static void addMeta(Div meta, String key, String value) {
        if (value == null || value.isBlank()) return;
        Span k = new Span(key);
        k.addClassName("k");
        meta.add(k, new Span(value));
    }

    private static Span statusPill(ReservationStatus st) {
        Span p = new Span(st == null ? "-" : switch (st) {
            case PAID -> "PAID";
            case CHECKED_IN -> "IN";
            case PAYMENT_PENDING -> "UNPAID";
        });
        p.addClassNames("pill", st == ReservationStatus.PAID ? "ok" : st == ReservationStatus.CHECKED_IN ? "done" : "unpaid");
        return p;
    }

    private static Button bigButton(String text, VaadinIcon icon, ButtonVariant... variants) {
        Button b = new Button(text, icon.create());
        b.addThemeVariants(ButtonVariant.LUMO_LARGE);
        b.addThemeVariants(variants);
        return b;
    }

    private static String joinNonBlank(String sep, String... parts) {
        List<String> keep = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isBlank() && !p.endsWith(" ")) keep.add(p);
        }
        return String.join(sep, keep);
    }

    private void updateSessionBadge() {
        sessionBadge.setText(sessionCount + " checked in on this device");
    }

    private void vibrate(int ms) {
        UI.getCurrent().getPage().executeJs("if (navigator.vibrate) navigator.vibrate($0);", ms);
    }

    private static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
    }

    private void notifyError(String msg) {
        Notification n = Notification.show(msg, 3500, Notification.Position.TOP_STRETCH);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
