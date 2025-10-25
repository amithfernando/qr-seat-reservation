package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.model.Setting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "Application settings management API")
public interface SettingController {

    /**
     * Get current application settings
     *
     * @return Current settings
     */
    @Operation(summary = "Get current settings", description = "Retrieves the current application settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved settings")
    })
    @GetMapping
    public ResponseEntity<Setting> getSettings();

    /**
     * Update application settings
     *
     * @param setting Updated settings
     * @return Updated settings
     */
    @Operation(summary = "Update settings", description = "Updates the application settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated settings"),
            @ApiResponse(responseCode = "400", description = "Invalid settings data")
    })
    @PutMapping
    public ResponseEntity<Setting> updateSettings(@RequestBody Setting setting);
}
