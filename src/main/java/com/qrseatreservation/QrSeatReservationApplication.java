package com.qrseatreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme(value = "qr-seat-reservation", variant = Lumo.DARK)
@Viewport("width=device-width, initial-scale=1, viewport-fit=cover")
@Push(transport = Transport.LONG_POLLING)
public class QrSeatReservationApplication implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addMetaTag("theme-color", "#1F1D2B");
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black-translucent");
    }

    public static void main(String[] args) {
        SpringApplication.run(QrSeatReservationApplication.class, args);
    }

}
