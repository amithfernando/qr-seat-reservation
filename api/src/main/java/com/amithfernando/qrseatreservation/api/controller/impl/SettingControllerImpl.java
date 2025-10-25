package com.amithfernando.qrseatreservation.api.controller.impl;

import com.amithfernando.qrseatreservation.api.controller.SettingController;
import com.amithfernando.qrseatreservation.api.model.Setting;
import com.amithfernando.qrseatreservation.api.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SettingControllerImpl implements SettingController {

    private final SettingService settingService;

    public SettingControllerImpl(SettingService settingService) {
        this.settingService = settingService;
    }


    public ResponseEntity<Setting> getSettings() {
        log.info("Fetching application settings");
        Setting setting = settingService.getSetting();
        return ResponseEntity.ok(setting);
    }


    public ResponseEntity<Setting> updateSettings(@RequestBody Setting setting) {
        log.info("Updating application settings");
        Setting updatedSetting = settingService.save(setting);
        return ResponseEntity.ok(updatedSetting);
    }
}
