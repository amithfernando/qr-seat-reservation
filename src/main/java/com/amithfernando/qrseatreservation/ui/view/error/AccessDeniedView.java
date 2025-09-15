package com.amithfernando.qrseatreservation.ui.view.error;

import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;

@Tag("div")
@PageTitle("Access Denied")
@ParentLayout(MainLayout.class)
public class AccessDeniedView extends RouteAccessDeniedError {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        String path = event.getLocation() != null ? "/" + String.join("/", event.getLocation().getSegments()) : "";
        getElement().setProperty("innerHTML",
                """
                <div style="display:flex;justify-content:center;align-items:center;min-height:50vh;">
                  <div style="text-align:center;">
                    <h2 style="margin:0 0 0.25em;">403 - Access denied</h2>
                    <p style="color: var(--lumo-secondary-text-color); margin: 0 0 1em;">
                      You don’t have permission to view this page%s
                    </p>
                    <div style="display:flex;gap:0.5rem;justify-content:center;">
                      <a href="/" style="text-decoration:none;">
                        <vaadin-button theme="primary">Go to Home</vaadin-button>
                      </a>
                      <a href="/login" style="text-decoration:none;">
                        <vaadin-button>Sign in with different user</vaadin-button>
                      </a>
                    </div>
                  </div>
                </div>
                """.formatted(path == null || path.isBlank() ? "." : " (" + path + ").")
        );
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
