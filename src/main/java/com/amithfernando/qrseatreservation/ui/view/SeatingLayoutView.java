package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.amithfernando.qrseatreservation.core.service.SettingService;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import com.amithfernando.qrseatreservation.ui.view.component.SeatCanvas;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
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

        // Generate seats
        List<TableDetail> allTables = tableDetailService.getAllTables();

        Setting setting = settingService.getSetting();
        // Canvas with tables + seats
        SeatCanvas canvas = new SeatCanvas(allTables,setting.getTableSize(),setting.getSeatSize(),setting.getNoOfColumns());

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

        addToContent(new HorizontalLayout(downloadBtn), canvas );
    }

}
