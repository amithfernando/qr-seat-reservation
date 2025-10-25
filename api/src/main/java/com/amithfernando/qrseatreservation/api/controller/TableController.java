package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.TableControllerImpl;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/tables")
public interface TableController {

    /**
     * Get all tables with their seat details
     *
     * @return List of all tables
     */
    @GetMapping
    public ResponseEntity<List<TableDetail>> getAllTables();

    /**
     * Get summary of all tables (total tables and seats)
     *
     * @return Table summary
     */
    @GetMapping("/summary")
    public ResponseEntity<TableDetailSummary> getTableSummary();

    /**
     * Create a new table with all seats marked as available
     *
     * @param tableDetail Table details
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<Void> createTable(@RequestBody TableDetail tableDetail);

    /**
     * Create a new table with specified available and unavailable seats
     *
     * @param request Request containing table creation details
     * @return Success response
     */
    @PostMapping("/custom")
    public ResponseEntity<Void> createCustomTable(@RequestBody TableControllerImpl.CreateTableRequest request);

    /**
     * Delete a table
     *
     * @param tableDetail Table to delete
     * @return Success response
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteTable(@RequestBody TableDetail tableDetail);

    /**
     * DTO for custom table creation
     */
    @lombok.Data
    public static class CreateTableRequest {
        private String tableName;
        private int noOfAvailableSeats;
        private int noOfUnavailableSeats;
        private String description;
    }
}
