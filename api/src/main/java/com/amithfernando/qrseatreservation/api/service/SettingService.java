package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.UpdateSettingRequest;
import com.amithfernando.qrseatreservation.api.model.Setting;
import com.amithfernando.qrseatreservation.api.repsitory.SettingRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Setting getSetting() {
        List<Setting> settings = settingRepository.findAll();
        if (settings.isEmpty()) {
            log.info("No settings found, creating default settings");
            return createDefaultSettings();
        }
        return settings.get(0);
    }

    @Transactional
    public Setting updateSettings(UpdateSettingRequest request) {
        List<Setting> existing = settingRepository.findAll();
        Setting setting;
        
        if (!existing.isEmpty()) {
            setting = existing.get(0);
            log.info("Updating existing settings with ID: {}", setting.getId());
        } else {
            setting = new Setting();
            log.info("Creating new settings");
        }

        // Update fields from request
        if (request.getEventName() != null) {
            setting.setEventName(request.getEventName());
        }
        if (request.getVenue() != null) {
            setting.setVenue(request.getVenue());
        }
        if (request.getTableSize() != null) {
            setting.setTableSize(request.getTableSize());
        }
        if (request.getSeatSize() != null) {
            setting.setSeatSize(request.getSeatSize());
        }
        if (request.getNoOfColumns() != null) {
            setting.setNoOfColumns(request.getNoOfColumns());
        }
        if (request.getFontSize() != null) {
            setting.setFontSize(request.getFontSize());
        }
        if (request.getQrX() != null) {
            setting.setQrX(request.getQrX());
        }
        if (request.getQrY() != null) {
            setting.setQrY(request.getQrY());
        }
        if (request.getTextX() != null) {
            setting.setTextX(request.getTextX());
        }
        if (request.getTextY() != null) {
            setting.setTextY(request.getTextY());
        }
        if (request.getTicketPrefix() != null) {
            setting.setTicketPrefix(request.getTicketPrefix());
        }
        if (request.getNoOfDigits() != null) {
            setting.setNoOfDigits(request.getNoOfDigits());
        }
        if (request.getMaxNoOfTickets() != null) {
            setting.setMaxNoOfTickets(request.getMaxNoOfTickets());
        }
        
        // Handle base64 encoded image
        if (request.getBaseImageBase64() != null && !request.getBaseImageBase64().isBlank()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(request.getBaseImageBase64());
                setting.setBaseImage(imageBytes);
                log.info("Base image updated, size: {} bytes", imageBytes.length);
            } catch (IllegalArgumentException e) {
                log.error("Failed to decode base64 image: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid base64 image data");
            }
        }

        Setting saved = settingRepository.save(setting);
        log.info("Settings updated successfully");
        return saved;
    }

    private Setting createDefaultSettings() {
        Setting setting = Setting.builder()
                .eventName("Default Event")
                .venue("Default Venue")
                .tableSize(100)
                .seatSize(24)
                .noOfColumns(3)
                .fontSize(14)
                .qrX(50)
                .qrY(50)
                .textX(50)
                .textY(120)
                .ticketPrefix("T")
                .noOfDigits(5)
                .maxNoOfTickets(100)
                .build();
        
        return settingRepository.save(setting);
    }
}
