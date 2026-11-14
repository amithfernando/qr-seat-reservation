package com.qrseatreservation.ui.view;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.*;

@Route("login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    public LoginView() {
        addClassName("login-view");
        setTitle("QR Seat Reservation");
        setDescription("Event operations console");
        setOpened(true);
        setAction("login"); // Spring Security endpoint
        setForgotPasswordButtonVisible(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Show error on auth failure (Spring Security appends ?error)
        boolean error = event.getLocation().getQueryParameters()
                .getParameters().containsKey("error");
        setError(error);
    }
}
