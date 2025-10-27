package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.TicketResponse;
import com.amithfernando.qrseatreservation.api.dto.TicketStatsResponse;
import com.amithfernando.qrseatreservation.api.enums.TicketStatus;
import com.amithfernando.qrseatreservation.api.model.Setting;
import com.amithfernando.qrseatreservation.api.model.Ticket;
import com.amithfernando.qrseatreservation.api.repsitory.TicketRepository;
import com.amithfernando.qrseatreservation.api.util.QrTicketGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SettingService settingService;

    public TicketService(TicketRepository ticketRepository, SettingService settingService) {
        this.ticketRepository = ticketRepository;
        this.settingService = settingService;
    }

    public void generateTicketNos() throws IOException {
        Setting setting = settingService.getSetting();
        log.info("Generating " + setting.getMaxNoOfTickets() + " tickets");
        for (int i = 0; i < setting.getMaxNoOfTickets(); i++) {
            String ticketNo = "";
            do {
                ticketNo = generateTicketNo();
            }
            while (ticketRepository.findByTicketNo(ticketNo) != null);
            QrTicketGenerator qrTicketGenerator = new QrTicketGenerator(
                    getBaseImage(),
                    setting.getFontSize(),
                    setting.getQrX(),
                    setting.getQrY(),
                    setting.getTextX(),
                    setting.getTextY()
            );
            BufferedImage bufferedImage = qrTicketGenerator.generateTicketImage(ticketNo);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            ticketRepository.save(new Ticket(ticketNo, TicketStatus.AVAILABLE, imageInByte));
        }
    }

    public BufferedImage generateQrTicketPreview(int fontSize, int qrX, int qrY, int textX, int textY, String ticketNo) {
        QrTicketGenerator qrTicketGenerator = new QrTicketGenerator(
                getBaseImage(),
                fontSize,
                qrX,
                qrY,
                textX,
                textY
        );
        BufferedImage bufferedImage = qrTicketGenerator.generateTicketImage(ticketNo);
        return bufferedImage;
    }

    private BufferedImage getBaseImage() {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(settingService.getSetting().getBaseImage())) {
            BufferedImage img = ImageIO.read(bais);
            if (img == null) {
                throw new IllegalArgumentException("Unsupported or corrupted image data");
            }
            return img;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to decode image", e);
        }


    }

    private String generateTicketNo() {
        Setting setting = settingService.getSetting();
        return setting.getTicketPrefix() + String.format("%0" + setting.getNoOfDigits() + "d", (int) (Math.random() * Math.pow(10, setting.getNoOfDigits())));
    }

    @Transactional
    public String getTicketNumber() {
        List<Ticket> availableTickets = ticketRepository.findTcicketByStatus(TicketStatus.AVAILABLE);
        if (availableTickets.size() > 0) {
            Ticket ticket = availableTickets.get(0);
            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);
            return ticket.getTicketNo();
        }
        return null;
    }


    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket findByTicketNo(String ticketNo) {
        return ticketRepository.findByTicketNo(ticketNo);
    }

    public byte[] createZipFromImages(List<Ticket> tickets) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Ticket ticket : tickets) {
                String entryName = (ticket.getTicketNo() + ".png");
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(ticket.getData());
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }


    /**
     * Get all tickets with metadata (no image data)
     */
    public List<TicketResponse> getAllTicketsMetadata() {
        return ticketRepository.findAll().stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    /**
     * Get ticket by number with metadata
     */
    public TicketResponse getTicketMetadata(String ticketNo) {
        Ticket ticket = ticketRepository.findByTicketNo(ticketNo);
        if (ticket == null) {
            throw new com.amithfernando.qrseatreservation.api.exception.TicketNotFoundException(ticketNo);
        }
        return mapToTicketResponse(ticket);
    }

    /**
     * Get tickets by status
     */
    public List<TicketResponse> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findTcicketByStatus(status).stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    /**
     * Get ticket statistics
     */
    public TicketStatsResponse getTicketStats() {
        List<Ticket> allTickets = ticketRepository.findAll();
        long total = allTickets.size();
        long available = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.AVAILABLE)
                .count();
        long used = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.USED)
                .count();

        double usagePercentage = total > 0 ? (used * 100.0 / total) : 0.0;

        return TicketStatsResponse.builder()
                .total(total)
                .available(available)
                .used(used)
                .usagePercentage(Math.round(usagePercentage * 100.0) / 100.0)
                .build();
    }

    /**
     * Generate tickets with custom count
     */
    public int generateTickets(Integer count) throws IOException {
        Setting setting = settingService.getSetting();
        int ticketsToGenerate = count != null ? count : setting.getMaxNoOfTickets();

        log.info("Generating {} tickets", ticketsToGenerate);
        int generated = 0;

        for (int i = 0; i < ticketsToGenerate; i++) {
            String ticketNo;
            do {
                ticketNo = generateTicketNo();
            } while (ticketRepository.findByTicketNo(ticketNo) != null);

            QrTicketGenerator qrTicketGenerator = new QrTicketGenerator(
                    getBaseImage(),
                    setting.getFontSize(),
                    setting.getQrX(),
                    setting.getQrY(),
                    setting.getTextX(),
                    setting.getTextY()
            );

            BufferedImage bufferedImage = qrTicketGenerator.generateTicketImage(ticketNo);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();

            ticketRepository.save(new Ticket(ticketNo, TicketStatus.AVAILABLE, imageInByte));
            generated++;
        }

        log.info("Successfully generated {} tickets", generated);
        return generated;
    }

    /**
     * Delete all tickets (use with caution)
     */
    @Transactional
    public int deleteAllTickets() {
        List<Ticket> allTickets = ticketRepository.findAll();
        int count = allTickets.size();
        ticketRepository.deleteAll();
        log.warn("Deleted all {} tickets", count);
        return count;
    }

    /**
     * Delete tickets by status
     */
    @Transactional
    public int deleteTicketsByStatus(TicketStatus status) {
        List<Ticket> tickets = ticketRepository.findTcicketByStatus(status);
        int count = tickets.size();
        ticketRepository.deleteAll(tickets);
        log.info("Deleted {} tickets with status {}", count, status);
        return count;
    }

    /**
     * Map Ticket entity to TicketResponse DTO (without image data)
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .status(ticket.getStatus())
                .hasImage(ticket.getData() != null && ticket.getData().length > 0)
                .imageSizeBytes(ticket.getData() != null ? ticket.getData().length : 0)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
