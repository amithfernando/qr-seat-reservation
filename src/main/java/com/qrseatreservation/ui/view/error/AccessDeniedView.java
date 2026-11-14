package com.qrseatreservation.ui.view.error;

import com.qrseatreservation.ui.view.layout.MainLayout;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouteAccessDeniedError;
import com.vaadin.flow.router.AccessDeniedException;
import jakarta.servlet.http.HttpServletResponse;

/** 403 page. Built from components (never innerHTML) so the requested path is always rendered as escaped text. */
@Tag("div")
@PageTitle("Access Denied")
@ParentLayout(MainLayout.class)
public class AccessDeniedView extends RouteAccessDeniedError {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        getElement().removeAllChildren();
        String path = event.getLocation() != null ? "/" + String.join("/", event.getLocation().getSegments()) : "";

        Div box = new Div();
        box.addClassName("error-page");
        H2 title = new H2("403 – Access denied");
        Paragraph text = new Paragraph("You don’t have permission to view this page"
                + (path.isBlank() || "/".equals(path) ? "." : " (" + path + ")."));
        Button home = new Button("Go to Home", VaadinIcon.HOME.create(), e -> UI.getCurrent().navigate(""));
        home.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button other = new Button("Sign in as another user", e -> UI.getCurrent().getPage().setLocation("/login"));
        Div actions = new Div(home, other);
        actions.addClassName("error-actions");
        box.add(title, text, actions);
        getElement().appendChild(box.getElement());
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
