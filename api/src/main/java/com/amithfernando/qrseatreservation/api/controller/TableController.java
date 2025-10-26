package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.CreateTableRequest;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import com.amithfernando.qrseatreservation.api.service.TableDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Tables", description = "Table and seat management API")
@Slf4j
public class TableController {

    private final TableDetailService tableDetailService;

    public TableController(TableDetailService tableDetailService) {
        this.tableDetailService = tableDetailService;
    }


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
    public ResponseEntity<List<TableDetail>> getAllTables() {
        log.info("Fetching all tables");
        List<TableDetail> tables = tableDetailService.getAllTables();
        return ResponseEntity.ok(tables);
    }


    /**
     * Get a summary of all tables (total tables and seats)
     *
     * @return Table summary
     */
    @Operation(summary = "Get table summary", description = "Retrieves summary statistics of all tables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved summary")
    })
    @GetMapping("/summary")
    public ResponseEntity<TableDetailSummary> getTableSummary() {
        log.info("Fetching table summary");
        TableDetailSummary summary = tableDetailService.getGetTableSummary();
        return ResponseEntity.ok(summary);
    }

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
    @PostMapping
    public ResponseEntity<Void> createTable(@RequestBody CreateTableRequest request) {
        log.info("Creating custom table: {}", request.getTableName());
        tableDetailService.createTable(
                request.getTableName(),
                request.getNoOfAvailableSeats(),
                request.getNoOfUnavailableSeats(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * Delete a table
     *
     * @param id Table to delete
     * @return Success response
     */
    @Operation(summary = "Delete table", description = "Deletes a table and its associated seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Table deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteTable( @PathVariable Long id) {
        log.info("Deleting table id: {}", id);
        tableDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
