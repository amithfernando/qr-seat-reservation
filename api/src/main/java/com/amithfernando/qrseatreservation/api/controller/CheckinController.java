package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.CheckInRequest;
import com.amithfernando.qrseatreservation.api.dto.CheckInResponse;
import com.amithfernando.qrseatreservation.api.dto.CheckInStats;
import com.amithfernando.qrseatreservation.api.dto.ErrorResponse;
import com.amithfernando.qrseatreservation.api.dto.TicketDetailsResponse;
import com.amithfernando.qrseatreservation.api.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@Tag(name = "Check-in", description = "Ticket check-in management API")
@Slf4j
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * Get ticket details by ticket number
     *
     * @param ticketNo Ticket number
     * @return Ticket details including check-in eligibility
     */
    @Operation(summary = "Get ticket details", 
               description = "Retrieves detailed information about a ticket including whether it can be checked in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket details"),
            @ApiResponse(responseCode = "404", description = "Ticket not found - TICKET_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/ticket/{ticketNo}")
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(@PathVariable String ticketNo) {
        log.info("Fetching ticket details for: {}", ticketNo);
        TicketDetailsResponse details = checkinService.getTicketDetails(ticketNo);
        return ResponseEntity.ok(details);
    }

    /**
     * Check in a ticket
     *
     * @param request Check-in request with ticket number
     * @return Check-in result
     */
    @Operation(summary = "Check in ticket", 
               description = "Checks in a ticket. Only allowed for PAID reservations that haven't been checked in yet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check-in processed (check success field for result)"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CheckInResponse> checkInTicket(@Valid @RequestBody CheckInRequest request) {
        log.info("Check-in request for ticket: {}", request.getTicketNo());
        CheckInResponse response = checkinService.checkInTicket(request.getTicketNo());
        return ResponseEntity.ok(response);
    }

    /**
     * Check in a ticket by ticket number (simplified endpoint)
     *
     * @param ticketNo Ticket number
     * @return Check-in result
     */
    @Operation(summary = "Check in ticket by number", 
               description = "Simplified endpoint to check in a ticket using path parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check-in processed (check success field for result)"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{ticketNo}")
    public ResponseEntity<CheckInResponse> checkInTicketByNumber(@PathVariable String ticketNo) {
        log.info("Check-in request for ticket: {}", ticketNo);
        CheckInResponse response = checkinService.checkInTicket(ticketNo);
        return ResponseEntity.ok(response);
    }

    /**
     * Get check-in statistics
     *
     * @return Check-in statistics
     */
    @Operation(summary = "Get check-in statistics", 
               description = "Retrieves overall check-in statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<CheckInStats> getCheckInStats() {
        log.info("Fetching check-in statistics");
        CheckInStats stats = checkinService.getCheckInStats();
        return ResponseEntity.ok(stats);
    }
}
