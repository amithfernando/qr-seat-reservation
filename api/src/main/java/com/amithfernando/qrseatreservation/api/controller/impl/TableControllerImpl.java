package com.amithfernando.qrseatreservation.api.controller.impl;

import com.amithfernando.qrseatreservation.api.controller.TableController;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import com.amithfernando.qrseatreservation.api.service.TableDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class TableControllerImpl implements TableController {

    private final TableDetailService tableDetailService;

    public TableControllerImpl(TableDetailService tableDetailService) {
        this.tableDetailService = tableDetailService;
    }


    public ResponseEntity<List<TableDetail>> getAllTables() {
        log.info("Fetching all tables");
        List<TableDetail> tables = tableDetailService.getAllTables();
        return ResponseEntity.ok(tables);
    }


    public ResponseEntity<TableDetailSummary> getTableSummary() {
        log.info("Fetching table summary");
        TableDetailSummary summary = tableDetailService.getGetTableSummary();
        return ResponseEntity.ok(summary);
    }


    public ResponseEntity<Void> createTable(@RequestBody TableDetail tableDetail) {
        log.info("Creating new table: {}", tableDetail.getTableName());
        tableDetailService.createTable(tableDetail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    public ResponseEntity<Void> createCustomTable(@RequestBody CreateTableRequest request) {
        log.info("Creating custom table: {}", request.getTableName());
        tableDetailService.createTable(
                request.getTableName(),
                request.getNoOfAvailableSeats(),
                request.getNoOfUnavailableSeats(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    public ResponseEntity<Void> deleteTable(@RequestBody TableDetail tableDetail) {
        log.info("Deleting table: {}", tableDetail.getTableName());
        tableDetailService.delete(tableDetail);
        return ResponseEntity.noContent().build();
    }


}
