package com.amithfernando.qrseatreservation.core.service;

import com.amithfernando.qrseatreservation.core.enums.TicketStatus;
import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.model.Ticket;
import com.amithfernando.qrseatreservation.core.repsitory.TicketRepository;
import com.amithfernando.qrseatreservation.core.util.QrTicketGenerator;
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
            while (ticketRepository.findByTicketNo(ticketNo) != null) ;
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
            ticketRepository.save(new Ticket( ticketNo, TicketStatus.AVAILABLE, imageInByte));
        }
    }

    public BufferedImage generateQrTicketPreview(int fontSize, int qrX, int qrY, int textX, int textY, String ticketNo){
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

    public  byte[] createZipFromImages(List<Ticket> tickets) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Ticket ticket : tickets) {
                String entryName =  (ticket.getTicketNo() + ".png");
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(ticket.getData());
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }
}
