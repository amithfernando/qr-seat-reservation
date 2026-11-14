package com.qrseatreservation.config;

import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.ui.view.EventManageView;
import com.qrseatreservation.ui.view.SelectEventView;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Forces every logged-in user to pick an event before any event page (all routes inside {@link MainLayout}) opens.
 * Runs before the target view is instantiated, so views never load data without a valid, authorised event selection.
 */
@Component
public class EventSelectionGuard implements VaadinServiceInitListener {

    private final EventContext eventContext;

    public EventSelectionGuard(EventContext eventContext) {
        this.eventContext = eventContext;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(init -> init.getUI().addBeforeEnterListener(this::check));
    }

    void check(BeforeEnterEvent event) {
        Class<?> target = event.getNavigationTarget();
        if (target == null || HasErrorParameter.class.isAssignableFrom(target)) return;
        Route route = target.getAnnotation(Route.class);
        if (route == null || route.layout() != MainLayout.class) return;
        // Event management works without a selected event (admin-only via @RolesAllowed), so an admin can always
        // create/activate an event even when none is selectable
        if (target == EventManageView.class) return;
        // Anonymous users are sent to the login page by Vaadin's own access control
        if (!EventContext.isAuthenticated()) return;
        if (!eventContext.hasValidSelection()) {
            event.forwardTo(SelectEventView.class);
        }
    }
}
