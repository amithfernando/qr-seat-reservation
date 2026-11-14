package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.service.SettingService;
import com.qrseatreservation.core.service.TableDetailService;
import com.qrseatreservation.ui.view.component.SeatCanvas;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

@Route(value = "seatingLayout", layout = MainLayout.class)
@RolesAllowed({"ADMIN","ENTRANCE"})
@PageTitle("Seating Layout")
@Slf4j
public class SeatingLayoutView extends PageLayout {

    private final TableDetailService tableDetailService;
    private final SettingService settingService;

    public SeatingLayoutView(TableDetailService tableDetailService, SettingService settingService) {
        super(VaadinIcon.LAYOUT,"Seating Layout");
        this.tableDetailService = tableDetailService;
        this.settingService = settingService;
        setSizeFull();
        try {
            buildLayout();
        } catch (Exception ex) {
            log.error("SeatingLayoutView failed to load — DB may be unavailable", ex);
            showDbError();
        }
    }

    private void buildLayout() {
        List<TableDetail> allTables = tableDetailService.getAllTables();

        Setting setting = settingService.getSetting();
        SeatCanvas canvas = new SeatCanvas(allTables, setting.getTableSize(), setting.getSeatSize(), setting.getNoOfColumns());
        canvas.setWidth("100%");
        canvas.getStyle().set("display", "block");

        com.vaadin.flow.component.html.Div canvasWrapper = new com.vaadin.flow.component.html.Div(canvas);
        canvasWrapper.getStyle()
                .set("overflow", "auto")
                .set("width", "100%")
                .set("flex", "1")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "4px")
                .set("background", "#fafafa");

        // Download button for the canvas
        Button downloadBtn = new Button("Download Layout", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addClickListener(e ->
                getUI().ifPresent(ui ->
                        ui.getPage().executeJs("""
                            const canvas = $0;
                            try {
                              // Offscreen canvas with white background
                              const exportCanvas = document.createElement('canvas');
                              exportCanvas.width = canvas.width;
                              exportCanvas.height = canvas.height;
                              const exctx = exportCanvas.getContext('2d');
                              exctx.fillStyle = '#ffffff';
                              exctx.fillRect(0, 0, exportCanvas.width, exportCanvas.height);
                              exctx.drawImage(canvas, 0, 0);

                              const dataUrl = exportCanvas.toDataURL('image/png');

                              const pad = n => String(n).padStart(2, '0');
                              const d = new Date();
                              const ts = `${d.getFullYear()}${pad(d.getMonth()+1)}${pad(d.getDate())}-${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}`;

                              const a = document.createElement('a');
                              a.href = dataUrl;
                              a.download = `seating-layout-${ts}.png`;
                              document.body.appendChild(a);
                              a.click();
                              a.remove();
                            } catch (err) {
                              console.error('Failed to download canvas', err);
                            }
                        """, canvas.getElement())
                )
        );

        addToContent(new HorizontalLayout(downloadBtn), canvasWrapper);
    }

    private void showDbError() {
        com.vaadin.flow.component.html.Span msg = new com.vaadin.flow.component.html.Span(
                "Could not load layout — database connection error. Please try again in a moment.");
        msg.getStyle().set("color", "var(--lumo-error-text-color)");
        Button retry = new Button("Retry", VaadinIcon.REFRESH.create());
        retry.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SeatingLayoutView.class)));
        addToContent(msg, retry);
    }

}
