package com.qrseatreservation.ui.view.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.List;

/** Shows rendered ticket images in the browser, one at a time, with previous/next and per-ticket download. */
public final class TicketPreviewDialog {

    public record Item(String ticketNo, byte[] jpg) {}

    private TicketPreviewDialog() {}

    public static Dialog open(String title, List<Item> items) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setWidth("min(720px, 95vw)");

        if (items == null || items.isEmpty()) {
            dialog.add(new Span("No tickets to preview."));
            dialog.getFooter().add(new Button("Close", e -> dialog.close()));
            dialog.open();
            return dialog;
        }

        Image image = new Image();
        image.getStyle().set("width", "100%").set("height", "auto").set("display", "block")
                .set("border", "1px solid var(--lumo-contrast-20pct)").set("border-radius", "6px");
        Span counter = new Span();
        counter.getStyle().set("font-weight", "600");
        Anchor download = new Anchor();
        download.getElement().setAttribute("download", true);
        Button downloadBtn = new Button("Download this ticket", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        download.add(downloadBtn);

        Button prev = new Button(VaadinIcon.ARROW_LEFT.create());
        Button next = new Button(VaadinIcon.ARROW_RIGHT.create());
        prev.addThemeVariants(ButtonVariant.LUMO_SMALL);
        next.addThemeVariants(ButtonVariant.LUMO_SMALL);

        int[] index = {0};
        Runnable show = () -> {
            Item it = items.get(index[0]);
            String file = it.ticketNo() + ".jpg";
            image.setSrc(new StreamResource(file, () -> new ByteArrayInputStream(it.jpg())));
            image.setAlt("Ticket " + it.ticketNo());
            download.setHref(new StreamResource(file, () -> new ByteArrayInputStream(it.jpg())));
            counter.setText(it.ticketNo() + (items.size() > 1 ? "  (" + (index[0] + 1) + " / " + items.size() + ")" : ""));
            prev.setEnabled(index[0] > 0);
            next.setEnabled(index[0] < items.size() - 1);
        };
        prev.addClickListener(e -> { index[0]--; show.run(); });
        next.addClickListener(e -> { index[0]++; show.run(); });
        prev.setVisible(items.size() > 1);
        next.setVisible(items.size() > 1);
        show.run();

        HorizontalLayout nav = new HorizontalLayout(prev, counter, next, download);
        nav.setWidthFull();
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.expand(counter);

        VerticalLayout content = new VerticalLayout(nav, image);
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
        return dialog;
    }
}
