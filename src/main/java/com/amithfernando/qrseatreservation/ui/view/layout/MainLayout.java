package com.amithfernando.qrseatreservation.ui.view.layout;

import com.amithfernando.qrseatreservation.ui.view.*;
import com.amithfernando.qrseatreservation.ui.view.ReservationManageView;
import com.amithfernando.qrseatreservation.ui.view.SellerManageView;
import com.amithfernando.qrseatreservation.ui.view.TableManageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.core.GrantedAuthority; // added

public class MainLayout extends AppLayout {

    private String appVersion="1.0.0";

    public MainLayout() {
        createHeader();
        createDrawer();
    }


    private void createHeader() {
        H3 logo = new H3("Seat Reservation");
        Span subtitle = new Span("Manage tables, reservations, and check-ins");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout brand = new VerticalLayout(logo, subtitle);
        brand.setPadding(false);
        brand.setSpacing(false);

        // Right-side user area: avatar + username + dropdown menu
        String name = getCurrentUsername();
        Avatar avatar = new Avatar();
        avatar.setName(name);
        avatar.setAbbreviation(getInitials(name));
        avatar.setThemeName("xsmall");

        Span username = new Span(name);
        username.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout userLabel = new HorizontalLayout(username,avatar);
        userLabel.setSpacing(true);
        userLabel.setPadding(false);
        userLabel.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        MenuBar userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = userMenu.addItem(userLabel);
        var submenu = root.getSubMenu();
        submenu.addItem(VaadinIcon.SIGN_OUT.create(), e -> {
            // Friendly confirm and logout
            com.vaadin.flow.component.dialog.Dialog confirm = new com.vaadin.flow.component.dialog.Dialog();
            confirm.setHeaderTitle("Sign out");
            confirm.add(new Span("Are you sure you want to sign out?"));
            Button cancel = new Button("Cancel", ev -> confirm.close());
            Button ok = new Button("Sign out", ev -> {
                confirm.close();
                logoutCurrentUser();
                UI.getCurrent().getPage().setLocation("/login");
            });
            ok.getElement().setAttribute("theme", "error primary");
            HorizontalLayout footer = new HorizontalLayout(cancel, ok);
            footer.setWidthFull();
            footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
            confirm.getFooter().add(footer);
            confirm.setModal(true);
            confirm.setDraggable(true);
            confirm.setResizable(false);
            confirm.open();
        }).setText("Logout");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                brand,
                userMenu
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.setPadding(true);
        header.setSpacing(true);
        header.expand(brand); // push user area to the right
        header.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        header.addClassNames("py-s", "px-m");

        addToNavbar(header);
    }
    // ... existing code ...
    private void createDrawer() {
        RouterLink home = createNavLink("Home", VaadinIcon.HOME, HomeView.class);
        home.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink tables = createNavLink("Tables", VaadinIcon.TABLE, TableManageView.class);
        RouterLink sellers = createNavLink("Sellers", VaadinIcon.USERS, SellerManageView.class);
        RouterLink reservation = createNavLink("Reservations", VaadinIcon.CALENDAR, ReservationManageView.class);
        RouterLink checkIn = createNavLink("Check In", VaadinIcon.QRCODE, CheckingView.class);
        RouterLink seatingLayout = createNavLink("Seating Layout", VaadinIcon.LAYOUT, SeatingLayoutView.class);
        RouterLink settings = createNavLink("Settings", VaadinIcon.COG, SettingsView.class);

        // Build the navigation based on roles
        boolean isAdmin = hasRole("ADMIN");
        boolean isEntrance = hasRole("ENTRANCE");

        VerticalLayout nav = new VerticalLayout();
        nav.setPadding(true);
        nav.setSpacing(false);
        nav.setWidthFull();

        // Section: Main (always visible)
        Span mainHdr = new Span("Main");
        styleSectionHeader(mainHdr);
        nav.add(mainHdr, home, new Hr());

        // Section: Management (ADMIN only)
        if (isAdmin) {
            Span mgmtHdr = new Span("Management");
            styleSectionHeader(mgmtHdr);
            RouterLink users = createNavLink("Users", VaadinIcon.USER, UserManageView.class);
            nav.add(mgmtHdr, tables, sellers, reservation, users, settings, new Hr());
        }

        // Section: Operations
        Span opsHdr = new Span("Operations");
        styleSectionHeader(opsHdr);
        nav.add(opsHdr);
        nav.add(checkIn, seatingLayout);


        Scroller scroller = new Scroller(nav);
        scroller.setSizeFull();

        // Footer with version, developer and GitHub link pinned to bottom
        VerticalLayout footer = createDrawerFooter();

        // Wrap scroller + footer so footer stays at bottom
        VerticalLayout drawerContent = new VerticalLayout(scroller, footer);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.expand(scroller);

        addToDrawer(drawerContent);
    }
    // ... existing code ...
    private RouterLink createNavLink(String text, VaadinIcon icon, Class<? extends Component> target) {
        Icon i = icon.create();
        i.setSize("var(--lumo-icon-size-m)");
        i.getStyle().set("margin-right", "var(--lumo-space-s)");

        Span label = new Span(text);
        HorizontalLayout content = new HorizontalLayout(i, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();
        content.getStyle().set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        RouterLink link = new RouterLink();
        link.add(content);
        link.setRoute(target);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        link.getElement().getThemeList().add("small");

        // Hover effect
        content.getElement().addEventListener("mouseover", e ->
                content.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        content.getElement().addEventListener("mouseout", e ->
                content.getStyle().remove("background-color"));

        return link;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "";
        }
        String name = auth.getName();
        if (name == null || "anonymousUser".equalsIgnoreCase(name)) {
            return "";
        }
        return name;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void logoutCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse(),
                    auth
            );
        }
    }

    // Check if the current user has a specific role
    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_" + roleName));
        }
        return false;
    }

    // CSS Style for section headers in the drawer
    private void styleSectionHeader(Span header) {
        header.getElement().getStyle()
              .set("font-weight", "600")
              .set("font-size", "var(--lumo-font-size-s)")
              .set("color", "var(--lumo-secondary-text-color)")
              .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xs)");
    }

    // Footer content: version, developer and GitHub link
    private VerticalLayout createDrawerFooter() {
        Span appInfo = new Span("QR Seat Reservation • v" + appVersion);
        appInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span devInfo = new Span("Developer: Amith Fernando");
        devInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Anchor github = new Anchor("https://github.com/amithfernando/qr-seat-reservation", "GitHub Project");
        github.setTarget("_blank");
        github.getElement().setAttribute("rel", "noopener");
        github.getStyle()
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout footer = new VerticalLayout(appInfo, devInfo, github);
        footer.setWidthFull();
        footer.setPadding(true);
        footer.setSpacing(false);
        footer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        footer.getStyle()
                .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        return footer;
    }
}
