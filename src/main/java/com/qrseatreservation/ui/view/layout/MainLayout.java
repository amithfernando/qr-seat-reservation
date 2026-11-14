package com.qrseatreservation.ui.view.layout;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.ui.view.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * App shell: top bar (brand, current event, user menu), side drawer navigation and, on phones,
 * a bottom tab bar with the most used destinations (Vaadin's touch-optimised navbar).
 */
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private static final String APP_VERSION = "1.1.0";

    private final EventContext eventContext;

    public MainLayout(EventContext eventContext) {
        this.eventContext = eventContext;
        addClassName("app-shell");
        setPrimarySection(Section.NAVBAR);
        createHeader();
        createDrawer();
        createBottomNav();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // On phones the drawer is an overlay: close it once the user picked a destination
        if (isOverlay()) setDrawerOpened(false);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("drawer-toggle");
        toggle.getElement().setAttribute("aria-label", "Menu");

        Div brand = new Div();
        brand.addClassName("brand");
        Div mark = new Div(VaadinIcon.TICKET.create());
        mark.addClassName("brand-mark");
        Span name = new Span("QR Seat");
        name.addClassName("brand-name");
        brand.add(mark, name);
        brand.getElement().setAttribute("role", "link");
        brand.addClickListener(e -> UI.getCurrent().navigate(HomeView.class));

        Div header = new Div();
        header.addClassName("app-header");
        Div spacer = new Div();
        spacer.addClassName("grow");
        header.add(toggle, brand, spacer, createEventIndicator(), createUserMenu());
        addToNavbar(header);
    }

    private Component createUserMenu() {
        String username = getCurrentUsername();
        Avatar avatar = new Avatar(username);
        avatar.setAbbreviation(getInitials(username));
        avatar.addClassName("user-avatar");
        Span label = new Span(username);
        label.addClassName("user-name");
        HorizontalLayout trigger = new HorizontalLayout(label, avatar);
        trigger.setSpacing(true);
        trigger.setPadding(false);
        trigger.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menu.addClassName("user-menu");
        var root = menu.addItem(trigger);
        root.getElement().setAttribute("aria-label", "User menu");
        root.getSubMenu().addItem(VaadinIcon.EXCHANGE.create(), e -> UI.getCurrent().navigate(SelectEventView.class))
                .add(new Span("Switch event"));
        root.getSubMenu().addItem(VaadinIcon.SIGN_OUT.create(), e -> confirmLogout())
                .add(new Span("Sign out"));
        return menu;
    }

    private void confirmLogout() {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Sign out");
        confirm.add(new Span("Are you sure you want to sign out?"));
        Button cancel = new Button("Cancel", ev -> confirm.close());
        Button ok = new Button("Sign out", ev -> {
            confirm.close();
            logoutCurrentUser();
            UI.getCurrent().getPage().setLocation("/login");
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirm.getFooter().add(cancel, ok);
        confirm.open();
    }

    /** Current event chip; archived events are flagged read-only. Tapping it opens the event picker. */
    private Component createEventIndicator() {
        Div box = new Div();
        box.addClassName("event-chip");
        box.getElement().setAttribute("role", "button");
        box.getElement().setAttribute("tabindex", "0");
        box.getElement().setAttribute("title", "Switch event");
        box.addClickListener(e -> UI.getCurrent().navigate(SelectEventView.class));
        Event event;
        try {
            event = eventContext.current();
        } catch (EventAccessException ex) {
            box.add(VaadinIcon.CALENDAR_BRIEFCASE.create(), new Span("Select event"));
            return box;
        }
        Span dot = new Span();
        dot.addClassNames("status-dot", event.isActive() ? "active" : event.isArchived() ? "archived" : "draft");
        Span name = new Span(event.getName());
        name.addClassName("event-chip-name");
        box.add(dot, name);
        if (event.isArchived()) {
            Span ro = new Span("Read-only");
            ro.addClassNames("chip", "danger", "event-chip-ro");
            box.add(ro);
        } else if (!event.isActive()) {
            Span draft = new Span("Draft");
            draft.addClassNames("chip", "accent", "event-chip-ro");
            box.add(draft);
        }
        Icon swap = VaadinIcon.ANGLE_DOWN.create();
        swap.addClassName("event-chip-caret");
        box.add(swap);
        return box;
    }

    // ── Drawer ────────────────────────────────────────────────────────────────

    private void createDrawer() {
        boolean isAdmin = hasRole("ADMIN");
        Div nav = new Div();
        nav.addClassName("drawer-nav");

        nav.add(section("Main"), link("Home", VaadinIcon.HOME, HomeView.class));

        if (isAdmin) {
            nav.add(section("Management"),
                    link("Events", VaadinIcon.CALENDAR_BRIEFCASE, EventManageView.class),
                    link("Reservations", VaadinIcon.CALENDAR, ReservationManageView.class),
                    link("Tables", VaadinIcon.TABLE, TableManageView.class),
                    link("Sellers", VaadinIcon.USERS, SellerManageView.class),
                    link("Users", VaadinIcon.USER, UserManageView.class));
            nav.add(section("Tickets"),
                    link("Ticket Categories", VaadinIcon.TAGS, TicketCategoryView.class),
                    link("Ticket Layout Editor", VaadinIcon.TICKET, TicketLayoutEditorView.class),
                    link("Settings", VaadinIcon.COG, SettingsView.class));
        }

        nav.add(section("Floor"), link("Seating Layout", VaadinIcon.LAYOUT, SeatingLayoutView.class));
        if (isAdmin) nav.add(link("Floor Layout Editor", VaadinIcon.GRID, FloorLayoutEditorView.class));
        nav.add(section("Operations"), link("Check In", VaadinIcon.QRCODE, CheckingView.class));
        if (isAdmin) nav.add(link("Reports", VaadinIcon.BAR_CHART, ReportView.class));

        Scroller scroller = new Scroller(nav);
        scroller.addClassName("drawer-scroller");

        Div footer = new Div();
        footer.addClassName("drawer-footer");
        footer.add(new Span("QR Seat Reservation · v" + APP_VERSION));

        Div drawer = new Div(scroller, footer);
        drawer.addClassName("drawer-content");
        addToDrawer(drawer);
    }

    /** Phone tab bar: the 4–5 destinations used most at an event. Shown only on small touch screens. */
    private void createBottomNav() {
        boolean isAdmin = hasRole("ADMIN");
        Div bar = new Div();
        bar.addClassName("bottom-nav");
        bar.add(tab("Home", VaadinIcon.HOME, HomeView.class));
        if (isAdmin) bar.add(tab("Bookings", VaadinIcon.CALENDAR, ReservationManageView.class));
        bar.add(tab("Scan", VaadinIcon.QRCODE, CheckingView.class));
        bar.add(tab("Seats", VaadinIcon.LAYOUT, SeatingLayoutView.class));
        if (isAdmin) bar.add(tab("Reports", VaadinIcon.BAR_CHART, ReportView.class));
        addToNavbar(true, bar);
    }

    private RouterLink tab(String text, VaadinIcon icon, Class<? extends Component> target) {
        RouterLink link = new RouterLink();
        link.addClassName("bottom-tab");
        link.setRoute(target);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        Span label = new Span(text);
        link.add(icon.create(), label);
        return link;
    }

    private static Span section(String text) {
        Span s = new Span(text);
        s.addClassName("nav-section-label");
        return s;
    }

    private RouterLink link(String text, VaadinIcon icon, Class<? extends Component> target) {
        RouterLink link = new RouterLink();
        link.addClassName("nav-link");
        link.setRoute(target);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        Icon i = icon.create();
        i.addClassName("nav-icon");
        link.add(i, new Span(text));
        return link;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equalsIgnoreCase(auth.getName())) return "";
        return auth.getName();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void logoutCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse(),
                    auth);
        }
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_" + roleName));
    }
}
