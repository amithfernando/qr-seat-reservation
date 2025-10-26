package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.dto.ErrorResponse;
import com.amithfernando.qrseatreservation.api.dto.UpdateSettingRequest;
import com.amithfernando.qrseatreservation.api.model.Setting;
import com.amithfernando.qrseatreservation.api.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "Application settings management API")
@Slf4j
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }


    /**
     * Get current application settings
     *
     * @return Current settings
     */
    @Operation(summary = "Get current settings", description = "Retrieves the current application settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved settings"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Setting> getSettings() {
        log.info("Fetching application settings");
        Setting setting = settingService.getSetting();
        return ResponseEntity.ok(setting);
    }

    /**
     * Update application settings
     *
     * @param request Updated settings
     * @return Updated settings
     */
    @Operation(summary = "Update settings", description = "Updates the application settings. All fields are optional - only provided fields will be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated settings"),
            @ApiResponse(responseCode = "400", description = "Invalid settings data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<Setting> updateSettings(@Valid @RequestBody UpdateSettingRequest request) {
        log.info("Updating application settings");
        Setting updatedSetting = settingService.updateSettings(request);
        return ResponseEntity.ok(updatedSetting);
    }
}
