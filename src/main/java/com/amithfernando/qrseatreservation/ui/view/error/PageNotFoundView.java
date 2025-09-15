package com.amithfernando.qrseatreservation.ui.view.error;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;

@Tag("div")
@PageTitle("Page Not Found")
@ParentLayout(com.amithfernando.qrseatreservation.ui.view.layout.MainLayout.class)
public class PageNotFoundView extends RouteNotFoundError {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        getElement().setProperty("innerHTML",
                """
                <div style="display:flex;justify-content:center;align-items:center;min-height:50vh;">
                  <div style="text-align:center;">
                    <h2 style="margin:0 0 0.25em;">404 - Page not found</h2>
                    <p style="color: var(--lumo-secondary-text-color); margin: 0 0 1em;">
                      The page you were looking for doesn’t exist or the link is broken.
                    </p>
                    <a href="/" style="text-decoration:none;">
                      <vaadin-button theme="primary">Go to Home</vaadin-button>
                    </a>
                  </div>
                </div>
                """
        );
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
