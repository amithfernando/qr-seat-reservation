package com.qrseatreservation.config;

import com.qrseatreservation.core.event.EventAccessException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shows event access violations (e.g. editing an archived event) as a notification instead of a generic error.
 */
@Component
@Slf4j
public class EventErrorHandlerInitializer implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(init -> init.getSession().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(ErrorEvent errorEvent) {
                EventAccessException eae = findCause(errorEvent.getThrowable());
                if (eae != null && UI.getCurrent() != null) {
                    log.warn("Event access denied: {}", eae.getMessage());
                    Notification n = Notification.show(eae.getMessage(), 5000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                super.error(errorEvent);
            }
        }));
    }

    private static EventAccessException findCause(Throwable t) {
        for (int depth = 0; t != null && depth < 10; t = t.getCause(), depth++) {
            if (t instanceof EventAccessException eae) return eae;
        }
        return null;
    }
}
