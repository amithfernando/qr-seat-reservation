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

        addToContent(canvas );
    }

}
