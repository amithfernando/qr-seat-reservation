package com.qrseatreservation.ui.view.layout;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Component;

public class PageLayout extends VerticalLayout {

    private final String pageHeaderTitle;
    private Icon pageIcon;
    // Content holder that pages can use to place their UI
    private final VerticalLayout content = new VerticalLayout();

    public PageLayout(Icon pageIcon,String pageHeaderTitle) {
        this.pageHeaderTitle = pageHeaderTitle;
        this.pageIcon = pageIcon;
        addClassName("page-shell");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addPageHeader(null);
        setupContentHolder();
    }

    // Optional: specify an icon for the title
    public PageLayout(VaadinIcon icon, String pageHeaderTitle) {
        this.pageHeaderTitle = pageHeaderTitle;
        addClassName("page-shell");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addPageHeader(icon);
        setupContentHolder();
    }

    private void addPageHeader(VaadinIcon icon) {
        H3 title = new H3(pageHeaderTitle);
        // Ensure white text on gradient background
        title.getStyle()
                .set("color", "#ffffff")
                .set("-webkit-text-fill-color", "#ffffff")
                .set("margin", "0");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("page-header");
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)");

        if (icon != null) {
            pageIcon = icon.create();
            pageIcon.setSize("22px");
            header.add(pageIcon, title);
        } else {
            header.add(title);
        }

        add(header);
    }

    // Initialize a friendly content holder (centered, padded, responsive)
    private void setupContentHolder() {
        content.setWidthFull();
        content.addClassName("page-content");
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("box-sizing", "border-box");
        add(content);
        expand(content);
    }

    // Public helpers for pages
    public void addToContent(Component... components) {
        content.add(components);
    }

    public void setContent(Component... components) {
        content.removeAll();
        content.add(components);
    }

    public VerticalLayout getContentWrapper() {
        return content;
    }
}
