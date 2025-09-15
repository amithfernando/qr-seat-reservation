package com.amithfernando.qrseatreservation.core.service;

import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.repsitory.SettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class SettingService {

    private final SettingRepository settingRepository;

    //seat layout
    @Value("${settings.seatingLayout.tableSize}")
    private  int tableSize;
    @Value("${settings.seatingLayout.seatSize}")
    private  int seatSize;
    @Value("${settings.seatingLayout.noOfColumns}")
    private  int noOfColumns;
    //qr ticket
    @Value("${settings.qrImage.baseImagePath}")
    private  String baseImagePath;
    @Value("${settings.qrImage.fontSize}")
    private  int fontSize;
    @Value("${settings.qrImage.qrX}")
    private  int qrX;
    @Value("${settings.qrImage.qrY}")
    private  int qrY;
    @Value("${settings.qrImage.textX}")
    private  int textX;
    @Value("${settings.qrImage.textY}")
    private  int textY;
    //tickets
    @Value("${settings.ticket.prefix}")
    private String ticketPrefix;
    @Value("${settings.ticket.noOfDigits}")
    private Integer noOfDigits;
    @Value("${settings.ticket.maxNoOfTickets}")
    private Integer maxNoOfTickets;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @PostConstruct
    public void initializeSettings(){
        log.info("Initializing settings");
        List<Setting> all = settingRepository.findAll();
        if(all.isEmpty())
            try{
                Setting setting = Setting.builder()
                        .tableSize(tableSize)
                        .seatSize(seatSize)
                        .noOfColumns(noOfColumns)
                        .baseImage(getBaseImage())
                        .fontSize(fontSize)
                        .qrX(qrX)
                        .qrY(qrY)
                        .textX(textX)
                        .textY(textY)
                        .ticketPrefix(ticketPrefix)
                        .noOfDigits(noOfDigits)
                        .maxNoOfTickets(maxNoOfTickets)
                        .build();
                settingRepository.save(setting);
                log.info("Settings initialized: {}", setting);
            }catch (Exception e){
                log.error("Error initializing settings", e);
            }
    }

    public Setting getSetting() {
        return settingRepository.findAll().get(0);
    }

    private byte[] getBaseImage() throws FileNotFoundException {
        if (baseImagePath == null || baseImagePath.isBlank()) {
            throw new IllegalStateException("Base image path is not configured.");
        }

        // 1) Try as a filesystem path
        Path path = java.nio.file.Paths.get(baseImagePath);
        if (Files.exists(path) && java.nio.file.Files.isRegularFile(path)) {
            try {
                return java.nio.file.Files.readAllBytes(path);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException("Failed to read base image from filesystem: " + baseImagePath, e);
            }
        }

        // 2) Fallback: try as a classpath resource
        String resourcePath = baseImagePath.startsWith("/") ? baseImagePath : "/" + baseImagePath;
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in != null) {
                try {
                    return in.readAllBytes();
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to read base image from classpath: " + resourcePath, e);
                }
            }
        } catch (java.io.IOException e) {
            // This catch only handles potential close() exceptions on the InputStream
            throw new UncheckedIOException("Failed while closing classpath stream for: " + resourcePath, e);
        }

        throw new FileNotFoundException("Base image not found at path or classpath: " + baseImagePath);
    }


    public Setting save(Setting setting) {
        settingRepository.save(setting);
        log.info("Settings updated: {}", setting);
        return setting;
    }
}
