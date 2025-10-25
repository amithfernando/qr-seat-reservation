package com.amithfernando.qrseatreservation.api.controller;

import com.amithfernando.qrseatreservation.api.model.Setting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/settings")
public interface SettingController {

    /**
     * Get current application settings
     *
     * @return Current settings
     */
    @GetMapping
    public ResponseEntity<Setting> getSettings();

    /**
     * Update application settings
     *
     * @param setting Updated settings
     * @return Updated settings
     */
    @PutMapping
    public ResponseEntity<Setting> updateSettings(@RequestBody Setting setting);
}
