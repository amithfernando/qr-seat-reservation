package com.amithfernando.qrseatreservation.ui.view.component;

import com.amithfernando.qrseatreservation.ui.dto.TableDetailSummary;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H3;

import static com.vaadin.flow.component.Tag.H5;

public class TableSummaryComponent extends VerticalLayout {

    private final H3 tableSummaryHeader = new H3("Table Summary");
    private final H4 totalNoOfTables = new H4("Total No of Tables: ");
    private final H4 totalNoOfAvailableSeats = new H4("Total No of Total Seats: ");

    public TableSummaryComponent(TableDetailSummary tableDetailSummary) {
        setPadding(false);
        add(tableSummaryHeader,new Hr(), totalNoOfTables, totalNoOfAvailableSeats);
        setContent(tableDetailSummary);
    }

    public void setContent(TableDetailSummary tableDetailSummary) {
        totalNoOfTables.setText("Total No of Tables: "+ tableDetailSummary.getTotalNoOfTables());
        totalNoOfAvailableSeats.setText("Total No of Total Seats: "+ tableDetailSummary.getTotalNoOfTotalSeats());
    }



}
