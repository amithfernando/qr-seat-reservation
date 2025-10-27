package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.*;
import com.amithfernando.qrseatreservation.api.enums.TicketStatus;
import com.amithfernando.qrseatreservation.api.model.Ticket;
import com.amithfernando.qrseatreservation.api.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Ticket generation and management API")
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Get all tickets metadata (without image data)
     *
     * @return List of all tickets
     */
    @Operation(summary = "Get all tickets", description = "Retrieves all tickets with metadata (image data excluded)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        log.info("Fetching all tickets metadata");
        List<TicketResponse> tickets = ticketService.getAllTicketsMetadata();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get ticket metadata by ticket number
     *
     * @param ticketNo Ticket number
     * @return Ticket metadata
     */
    @Operation(summary = "Get ticket by number", description = "Retrieves ticket metadata by ticket number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found - TICKET_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{ticketNo}")
    public ResponseEntity<TicketResponse> getTicketByNumber(@PathVariable String ticketNo) {
        log.info("Fetching ticket metadata: {}", ticketNo);
        TicketResponse ticket = ticketService.getTicketMetadata(ticketNo);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Get tickets by status
     *
     * @param status Ticket status
     * @return List of tickets with specified status
     */
    @Operation(summary = "Get tickets by status", description = "Retrieves all tickets with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponse>> getTicketsByStatus(@PathVariable TicketStatus status) {
        log.info("Fetching tickets with status: {}", status);
        List<TicketResponse> tickets = ticketService.getTicketsByStatus(status);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get ticket statistics
     *
     * @return Ticket statistics
     */
    @Operation(summary = "Get ticket statistics", description = "Retrieves overall ticket statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<TicketStatsResponse> getTicketStats() {
        log.info("Fetching ticket statistics");
        TicketStatsResponse stats = ticketService.getTicketStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Download ticket image
     *
     * @param ticketNo Ticket number
     * @return Ticket image
     */
    @Operation(summary = "Download ticket image", description = "Downloads the QR ticket image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved image"),
            @ApiResponse(responseCode = "404", description = "Ticket not found - TICKET_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{ticketNo}/image")
    public ResponseEntity<byte[]> downloadTicketImage(@PathVariable String ticketNo) {
        log.info("Downloading ticket image: {}", ticketNo);
        Ticket ticket = ticketService.findByTicketNo(ticketNo);

        if (ticket == null) {
            throw new com.amithfernando.qrseatreservation.api.exception.TicketNotFoundException(ticketNo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("attachment", ticketNo + ".jpg");

        return ResponseEntity.ok()
                .headers(headers)
                .body(ticket.getData());
    }

    /**
     * Generate tickets
     *
     * @param request Generation request (optional count)
     * @return Number of tickets generated
     */
    @Operation(summary = "Generate tickets",
            description = "Generates new tickets. Uses count from request or settings if not provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tickets generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTickets(@Valid @RequestBody(required = false) GenerateTicketsRequest request) {
        log.info("Generating tickets, count: {}", request != null ? request.getCount() : "from settings");
        try {
            int count = ticketService.generateTickets(request != null ? request.getCount() : null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Tickets generated successfully",
                            "count", count
                    ));
        } catch (IOException e) {
            log.error("Failed to generate tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to generate tickets: " + e.getMessage()
                    ));
        }
    }

    /**
     * Generate tickets (simple endpoint without request body)
     *
     * @return Number of tickets generated
     */
    @Operation(summary = "Generate tickets (simple)",
            description = "Generates tickets using settings configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tickets generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/generate/default")
    public ResponseEntity<Map<String, Object>> generateTicketsDefault() {
        log.info("Generating tickets using default settings");
        try {
            ticketService.generateTicketNos();
            TicketStatsResponse stats = ticketService.getTicketStats();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Tickets generated successfully",
                            "stats", stats
                    ));
        } catch (IOException e) {
            log.error("Failed to generate tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to generate tickets: " + e.getMessage()
                    ));
        }
    }

    /**
     * Delete all tickets
     *
     * @return Number of tickets deleted
     */
    @Operation(summary = "Delete all tickets",
            description = "Deletes all tickets from the system (use with caution)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllTickets() {
        log.warn("Deleting all tickets");
        int count = ticketService.deleteAllTickets();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All tickets deleted",
                "count", count
        ));
    }

    /**
     * Delete tickets by status
     *
     * @param status Ticket status
     * @return Number of tickets deleted
     */
    @Operation(summary = "Delete tickets by status",
            description = "Deletes all tickets with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> deleteTicketsByStatus(@PathVariable TicketStatus status) {
        log.info("Deleting tickets with status: {}", status);
        int count = ticketService.deleteTicketsByStatus(status);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tickets deleted",
                "status", status,
                "count", count
        ));
    }
}