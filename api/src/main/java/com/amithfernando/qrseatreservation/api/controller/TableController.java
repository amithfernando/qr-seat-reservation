package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.CreateTableRequest;
import com.amithfernando.qrseatreservation.api.dto.ErrorResponse;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import com.amithfernando.qrseatreservation.api.service.TableDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tables"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TableDetail>> getAllTables() {
        log.info("Fetching all tables");
        List<TableDetail> tables = tableDetailService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    /**
     * Get a specific table by ID
     *
     * @param id Table ID
     * @return Table details
     */
    @Operation(summary = "Get table by ID", description = "Retrieves a specific table by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved table"),
            @ApiResponse(responseCode = "404", description = "Table not found - TABLE_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TableDetail> getTableById(@PathVariable Long id) {
        log.info("Fetching table with id: {}", id);
        TableDetail table = tableDetailService.getTableById(id);
        return ResponseEntity.ok(table);
    }

    /**
     * Get a table by name
     *
     * @param name Table name
     * @return Table details
     */
    @Operation(summary = "Get table by name", description = "Retrieves a specific table by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved table"),
            @ApiResponse(responseCode = "404", description = "Table not found - TABLE_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<TableDetail> getTableByName(@RequestParam String name) {
        log.info("Fetching table with name: {}", name);
        TableDetail table = tableDetailService.getTableByName(name);
        return ResponseEntity.ok(table);
    }


    /**
     * Get a summary of all tables (total tables and seats)
     *
     * @return Table summary
     */
    @Operation(summary = "Get table summary", description = "Retrieves summary statistics of all tables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved summary"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
            @ApiResponse(responseCode = "400", description = "Invalid table data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Table already exists - TABLE_ALREADY_EXISTS",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
     * Update an existing table
     *
     * @param id Table ID
     * @param request Updated table details
     * @return Success response
     */
    @Operation(summary = "Update table", description = "Updates an existing table's details. Cannot update tables with active reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Table updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid table data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Table not found - TABLE_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TableDetail> updateTable(@PathVariable Long id, @RequestBody CreateTableRequest request) {
        log.info("Updating table id: {}", id);
        TableDetail updatedTable = tableDetailService.updateTable(id, request);
        return ResponseEntity.ok(updatedTable);
    }


    /**
     * Delete a table
     *
     * @param id Table ID to delete
     * @return Success response
     */
    @Operation(summary = "Delete table", description = "Deletes a table and its associated seats. Cannot delete tables with active reservations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Table deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found - TABLE_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Table has active reservations - TABLE_HAS_RESERVATIONS",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        log.info("Deleting table id: {}", id);
        tableDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a table can be modified (has no active reservations)
     *
     * @param id Table ID to check
     * @return Boolean indicating if table can be modified
     */
    @Operation(summary = "Check if table can be modified", description = "Checks if a table has any active reservations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked table status"),
            @ApiResponse(responseCode = "404", description = "Table not found - TABLE_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/can-modify")
    public ResponseEntity<Boolean> canModifyTable(@PathVariable Long id) {
        log.info("Checking if table id {} can be modified", id);
        boolean canModify = tableDetailService.canModifyTable(id);
        return ResponseEntity.ok(canModify);
    }
}
