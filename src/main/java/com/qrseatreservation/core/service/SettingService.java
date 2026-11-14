package com.qrseatreservation.core.service;

import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.repository.SettingRepository;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.EventRepository;
import org.springframework.transaction.annotation.Transactional;
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
    @Value("${settings.qrImage.qrBorder:1}")
    private  int qrBorder;
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

    private final EventContext eventContext;
    private final EventRepository eventRepository;

    public SettingService(SettingRepository settingRepository, EventContext eventContext, EventRepository eventRepository) {
        this.settingRepository = settingRepository;
        this.eventContext = eventContext;
        this.eventRepository = eventRepository;
    }

    /** Creates the settings for a new event from the application.yaml defaults. */
    public Setting createDefaultSetting(Event event) {
        byte[] baseImage = null;
        try {
            baseImage = getBaseImage();
        } catch (Exception e) {
            log.warn("Default base ticket image not available ({}); upload one in Settings", e.getMessage());
        }
        Setting setting = Setting.builder()
                .event(event)
                .eventName(event.getName())
                .venue(event.getVenue())
                .tableSize(tableSize)
                .seatSize(seatSize)
                .noOfColumns(noOfColumns)
                .baseImage(baseImage)
                .fontSize(fontSize)
                .qrBorder(qrBorder)
                .qrX(qrX)
                .qrY(qrY)
                .textX(textX)
                .textY(textY)
                .tableSeatFontSize(24)
                .tableSeatX(0)
                .tableSeatY(0)
                .ticketPrefix(ticketPrefix)
                .noOfDigits(noOfDigits)
                .maxNoOfTickets(maxNoOfTickets)
                .build();
        settingRepository.save(setting);
        log.info("Default settings created for event id={}", event.getId());
        return setting;
    }

    /** Creates the settings for a new event by copying the ticket template and layout of another event. */
    public Setting copySetting(Setting source, Event target) {
        Setting copy = Setting.builder()
                .event(target)
                .eventName(target.getName())
                .venue(target.getVenue())
                .tableSize(source.getTableSize())
                .seatSize(source.getSeatSize())
                .noOfColumns(source.getNoOfColumns())
                .baseImage(source.getBaseImage())
                .fontSize(source.getFontSize())
                .qrSize(source.getQrSize())
                .qrBorder(source.getQrBorder())
                .qrX(source.getQrX())
                .qrY(source.getQrY())
                .textX(source.getTextX())
                .textY(source.getTextY())
                .tableSeatFontSize(source.getTableSeatFontSize())
                .tableSeatX(source.getTableSeatX())
                .tableSeatY(source.getTableSeatY())
                .ticketNoColor(source.getTicketNoColor())
                .tableSeatColor(source.getTableSeatColor())
                .ticketPrefix(source.getTicketPrefix())
                .noOfDigits(source.getNoOfDigits())
                .maxNoOfTickets(source.getMaxNoOfTickets())
                .sampleTicketNo(source.getSampleTicketNo())
                .sampleTable(source.getSampleTable())
                .sampleSeat(source.getSampleSeat())
                .build();
        settingRepository.save(copy);
        log.info("Settings copied from event id={} to event id={}", source.getEvent().getId(), target.getId());
        return copy;
    }

    /** Settings of the current event. */
    public Setting getSetting() {
        return getSetting(eventContext.currentId());
    }

    public Setting getSetting(Long eventId) {
        return settingRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("No settings found for event id=" + eventId));
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


    @Transactional
    public Setting save(Setting setting) {
        Event event = eventContext.requireEditable();
        EventContext.assertBelongsTo(setting.getEvent(), event.getId());
        settingRepository.save(setting);
        // Keep the event record's name/venue in sync with the display values edited in Settings
        if (setting.getEventName() != null && !setting.getEventName().isBlank()) {
            event.setName(setting.getEventName().trim());
        }
        event.setVenue(setting.getVenue());
        event.setUpdatedBy(EventContext.currentUser());
        eventRepository.save(event);
        log.info("Settings updated for event id={}", event.getId());
        return setting;
    }
}
