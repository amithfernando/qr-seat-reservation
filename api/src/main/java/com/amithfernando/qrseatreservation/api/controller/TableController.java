package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.controller.impl.TableControllerImpl;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/tables")
@Tag(name = "Tables", description = "Table and seat management API")
public interface TableController {

    /**
     * Get all tables with their seat details
     *
     * @return List of all tables
     */
    @Operation(summary = "Get all tables", description = "Retrieves all tables with their seat details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tables")
    })
    @GetMapping
    public ResponseEntity<List<TableDetail>> getAllTables();

    /**
     * Get summary of all tables (total tables and seats)
     *
     * @return Table summary
     */
    @Operation(summary = "Get table summary", description = "Retrieves summary statistics of all tables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved summary")
    })
    @GetMapping("/summary")
    public ResponseEntity<TableDetailSummary> getTableSummary();

    /**
     * Create a new table with all seats marked as available
     *
     * @param tableDetail Table details
     * @return Success response
     */
    @Operation(summary = "Create table", description = "Creates a new table with all seats marked as available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Table created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid table data")
    })
    @PostMapping
    public ResponseEntity<Void> createTable(@RequestBody TableDetail tableDetail);

    /**
     * Create a new table with specified available and unavailable seats
     *
     * @param request Request containing table creation details
     * @return Success response
     */
    @Operation(summary = "Create custom table", description = "Creates a table with specified available and unavailable seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Table created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid table data")
    })
    @PostMapping("/custom")
    public ResponseEntity<Void> createCustomTable(@RequestBody TableControllerImpl.CreateTableRequest request);

    /**
     * Delete a table
     *
     * @param tableDetail Table to delete
     * @return Success response
     */
    @Operation(summary = "Delete table", description = "Deletes a table and its associated seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Table deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
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
