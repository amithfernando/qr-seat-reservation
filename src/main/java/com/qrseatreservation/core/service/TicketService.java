package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import java.util.HashSet;
import java.util.Set;

import com.qrseatreservation.core.cache.BaseImageCache;
import com.qrseatreservation.core.enums.TicketStatus;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.model.Ticket;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.TicketRepository;
import com.qrseatreservation.core.util.QrTicketGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SettingService settingService;
    private final BaseImageCache baseImageCache;
    private final SeatReservationRepository seatReservationRepository;
    private static final int BATCH_SIZE = 5; // Small batches to avoid OOM on MySQL with large image blobs

    private final EventContext eventContext;

    public TicketService(TicketRepository ticketRepository, SettingService settingService, BaseImageCache baseImageCache,
                          SeatReservationRepository seatReservationRepository, EventContext eventContext) {
        this.eventContext = eventContext;
        this.ticketRepository = ticketRepository;
        this.settingService = settingService;
        this.baseImageCache = baseImageCache;
        this.seatReservationRepository = seatReservationRepository;
    }

    // No outer @Transactional — each ticketRepository.saveAll() runs its own short transaction
    public void generateTicketNos() throws IOException {
        generateTicketNos(eventContext.requireEditable());
    }

    /** Explicit-event variant for work without a UI session (e.g. background or maintenance tasks). */
    public void generateTicketNos(Long eventId) throws IOException {
        generateTicketNos(EventContext.requireEditable(settingService.getSetting(eventId).getEvent()));
    }

    private void generateTicketNos(Event event) throws IOException {
        Setting setting = settingService.getSetting(event.getId());
        int maxTickets = setting.getMaxNoOfTickets();
        long existing = ticketRepository.countByEventId(event.getId());
        int toGenerate = (int) Math.max(0, maxTickets - existing);
        if (toGenerate == 0) {
            log.info("Already at max tickets ({}), nothing to generate", maxTickets);
            return;
        }
        log.info("Generating {} tickets (existing={}, max={})", toGenerate, existing, maxTickets);

        BufferedImage baseImage = baseImageCache.getBaseImage(setting.getBaseImage());
        // Create generator once — generateTicketImage now clones internally
        QrTicketGenerator qrTicketGenerator = newGenerator(setting, baseImage);

        List<Ticket> ticketBatch = new ArrayList<>(BATCH_SIZE);
        Set<String> generatedThisRun = new HashSet<>();
        for (int i = 0; i < toGenerate; i++) {
            String ticketNo = generateUniqueTicketNo(setting, generatedThisRun);
            BufferedImage bufferedImage = qrTicketGenerator.generateTicketImage(ticketNo, "");
            byte[] imageInByte = convertImageToBytes(bufferedImage);
            Ticket ticket = new Ticket(ticketNo, TicketStatus.AVAILABLE, imageInByte);
            ticket.setEvent(event);
            ticketBatch.add(ticket);

            if (ticketBatch.size() >= BATCH_SIZE || i == toGenerate - 1) {
                ticketRepository.saveAll(ticketBatch); // short transaction per batch
                ticketBatch.clear();
                log.info("Generated {}/{} tickets", i + 1, toGenerate);
            }
        }
        log.info("Successfully generated {} tickets", toGenerate);
    }

    /**
     * Re-renders the image of every existing ticket using the current layout settings,
     * without creating new ticket numbers or touching ticket status. Table/seat labels
     * are pulled from the ticket's reservation, if any (blank otherwise, same as generation).
     */
    // No outer @Transactional — each page's saveAll runs its own short transaction
    public int regenerateAllTicketImages() throws IOException {
        return regenerateAllTicketImages(null);
    }

    public int regenerateAllTicketImages(Consumer<Integer> progressCallback) throws IOException {
        return regenerateAllTicketImages(eventContext.currentId(), progressCallback);
    }

    /** Explicit-event variant for background threads, where no UI session is available to resolve the event. */
    public int regenerateAllTicketImages(Long eventId, Consumer<Integer> progressCallback) throws IOException {
        Setting setting = settingService.getSetting(eventId);
        EventContext.requireEditable(setting.getEvent());
        BufferedImage baseImage = baseImageCache.getBaseImage(setting.getBaseImage());
        Map<String, String> tableSeatByTicketNo = buildTableSeatLabelMap(eventId);
        // Create generator once — generateTicketImage clones internally
        QrTicketGenerator qrTicketGenerator = newGenerator(setting, baseImage);

        int pageNo = 0, total = 0;
        Page<Ticket> page;
        do {
            page = ticketRepository.findByEventId(eventId, PageRequest.of(pageNo, BATCH_SIZE, Sort.by("id")));
            for (Ticket ticket : page) {
                String tableSeat = tableSeatByTicketNo.getOrDefault(ticket.getTicketNo(), "");
                BufferedImage rendered = qrTicketGenerator.generateTicketImage(ticket.getTicketNo(), tableSeat);
                ticket.setData(convertImageToBytes(rendered));
            }
            ticketRepository.saveAll(page.getContent()); // short transaction per page
            total += page.getNumberOfElements();
            if (progressCallback != null) progressCallback.accept(total);
            log.info("Regenerated {} ticket images so far", total);
            pageNo++;
        } while (page.hasNext());

        log.info("Successfully regenerated {} ticket images", total);
        return total;
    }

    private Map<String, String> buildTableSeatLabelMap(Long eventId) {
        Map<String, String> map = new HashMap<>();
        for (SeatReservation sr : seatReservationRepository.findAllWithSeatDetails(eventId)) {
            if (sr.getTicketNo() == null || sr.getSeatDetail() == null) continue;
            String table = sr.getSeatDetail().getTableDetail() != null
                    ? sr.getSeatDetail().getTableDetail().getTableName() : "";
            String seat = sr.getSeatDetail().getSeatNo() != null ? sr.getSeatDetail().getSeatNo() : "";
            map.put(sr.getTicketNo(), (table + "  " + seat).trim());
        }
        return map;
    }

    /** Unique across ALL events (not just this one), so old QR codes can never match a ticket of a new event. */
    private String generateUniqueTicketNo(Setting setting, Set<String> generatedThisRun) {
        String ticketNo;
        int attempts = 0;
        do {
            ticketNo = generateTicketNo(setting);
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Unable to generate unique ticket number after 100 attempts");
            }
        } while (generatedThisRun.contains(ticketNo) || ticketRepository.existsAnywhere(ticketNo));
        generatedThisRun.add(ticketNo);
        return ticketNo;
    }
    
    private byte[] convertImageToBytes(BufferedImage image) throws IOException {
        return toJpeg(image);
    }

    /**
     * Encodes a ticket as JPEG. JPEG has no alpha channel and ImageIO refuses to write ARGB images
     * (e.g. a transparent PNG background), so those are flattened onto white first.
     */
    static byte[] toJpeg(BufferedImage image) throws IOException {
        BufferedImage rgb = image;
        if (image.getColorModel().hasAlpha() || image.getType() == BufferedImage.TYPE_CUSTOM) {
            rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = rgb.createGraphics();
            try {
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.drawImage(image, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (!ImageIO.write(rgb, "jpg", baos)) {
                throw new IOException("No ImageIO writer found for ticket image format: jpg");
            }
            return baos.toByteArray();
        }
    }
    
    private BufferedImage cloneImage(BufferedImage source) {
        if (source == null) {
            throw new IllegalArgumentException("Base ticket image could not be loaded");
        }
        int imageType = source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_RGB : source.getType();
        BufferedImage clone = new BufferedImage(
            source.getWidth(), 
            source.getHeight(), 
            imageType
        );
        var graphics = clone.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return clone;
    }

    public BufferedImage generateQrTicketPreview(int fontSize, int qrSize, int qrX, int qrY, int textX, int textY,
                                                  int qrBorder, Integer tableSeatFontSize, Integer tableSeatX, Integer tableSeatY,
                                                  String seatLabel, String ticketNo) {
        QrTicketGenerator qrTicketGenerator = new QrTicketGenerator(
                cloneImage(getBaseImage()),
                fontSize, qrSize, qrX, qrY, textX, textY, qrBorder,
                tableSeatFontSize, tableSeatX, tableSeatY
        );
        return qrTicketGenerator.generateTicketImage(ticketNo, seatLabel);
    }

    private BufferedImage getBaseImage() {
        Setting setting = settingService.getSetting();
        return baseImageCache.getBaseImage(setting.getBaseImage());
    }

    /** Generates a personalised ticket image on-the-fly (used when pre-generated ticket is missing). */
    public byte[] generateOnTheFly(String ticketNo, String tableSeatText) throws IOException {
        Setting setting = settingService.getSetting();
        return renderTicket(setting, ticketNo, tableSeatText);
    }

    /**
     * Renders a single ticket exactly as generation/regeneration/download would, using the given
     * (possibly unsaved) layout values. Used for in-app previews so no ZIP download is needed.
     */
    public byte[] renderTicket(Setting layout, String ticketNo, String tableSeatText) throws IOException {
        BufferedImage img = newGenerator(layout, getBaseImage())
                .generateTicketImage(ticketNo, tableSeatText != null ? tableSeatText : "");
        if (img == null) throw new IOException("Failed to render ticket " + ticketNo);
        return convertImageToBytes(img);
    }

    private static QrTicketGenerator newGenerator(Setting setting, BufferedImage baseImage) {
        if (baseImage == null) {
            throw new IllegalArgumentException("Base ticket image could not be loaded");
        }
        return new QrTicketGenerator(
                baseImage,
                setting.getFontSize(), setting.getQrSize(),
                setting.getQrX(), setting.getQrY(),
                setting.getTextX(), setting.getTextY(),
                setting.getQrBorder(),
                setting.getTableSeatFontSize(),
                setting.getTableSeatX(), setting.getTableSeatY(),
                setting.getTicketNoColor(), setting.getTableSeatColor());
    }

    /** True if the ticket number has the configured prefix followed by exactly noOfDigits digits. */
    public static boolean matchesFormat(String ticketNo, String prefix, Integer noOfDigits) {
        if (ticketNo == null || noOfDigits == null || noOfDigits < 1) return false;
        String p = prefix != null ? prefix : "";
        return Pattern.matches(Pattern.quote(p) + "\\d{" + noOfDigits + "}", ticketNo);
    }

    /** Unused (AVAILABLE) tickets whose number doesn't match the current prefix / digit settings. */
    public List<String> findUnusedTicketsWithOldFormat() {
        Setting setting = settingService.getSetting();
        return ticketRepository.findTicketInfoByStatus(setting.getEvent().getId(), TicketStatus.AVAILABLE).stream()
                .map(TicketRepository.TicketInfo::getTicketNo)
                .filter(no -> !matchesFormat(no, setting.getTicketPrefix(), setting.getNoOfDigits()))
                .toList();
    }

    /** Deletes unused tickets with an outdated number format. Assigned/used tickets are never touched. */
    public int deleteUnusedTicketsWithOldFormat() {
        Event event = eventContext.requireEditable();
        Setting setting = settingService.getSetting(event.getId());
        List<Long> ids = ticketRepository.findTicketInfoByStatus(event.getId(), TicketStatus.AVAILABLE).stream()
                .filter(t -> !matchesFormat(t.getTicketNo(), setting.getTicketPrefix(), setting.getNoOfDigits()))
                .map(TicketRepository.TicketInfo::getId)
                .toList();
        if (!ids.isEmpty()) ticketRepository.deleteAllByIdInBatch(ids);
        log.info("Deleted {} unused tickets with outdated number format", ids.size());
        return ids.size();
    }

    private String generateTicketNo(Setting setting) {
        int upperBound = (int) Math.pow(10, setting.getNoOfDigits());
        int nextNumber = ThreadLocalRandom.current().nextInt(upperBound);
        return setting.getTicketPrefix() + String.format("%0" + setting.getNoOfDigits() + "d", nextNumber);
    }

    /** Returns assigned (USED) tickets of the event to the free pool, e.g. after their reservation was deleted. */
    @Transactional
    public int releaseTickets(Long eventId, java.util.Collection<String> ticketNos) {
        if (eventId == null || ticketNos == null || ticketNos.isEmpty()) return 0;
        return ticketRepository.releaseUsed(eventId, ticketNos, java.time.LocalDateTime.now());
    }

    /** Takes the next free ticket of the given event (row-locked so concurrent reservations never share one). */
    @Transactional
    public String getTicketNumber(Long eventId) {
        Ticket ticket = ticketRepository.findFirstAvailableTicketForUpdate(eventId, TicketStatus.AVAILABLE.name());
        
        if (ticket != null) {
            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);
            return ticket.getTicketNo();
        }
        return null;
    }


    public byte[] createZipFromImages(List<Ticket> tickets) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Ticket ticket : tickets) {
                if (ticket == null || ticket.getData() == null) continue;
                String entryName = ticket.getTicketNo() + ".jpg";
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(ticket.getData());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
    
    /** First {@code limit} tickets (by id) including their stored images, for in-app preview. */
    public List<Ticket> findFirstTickets(int limit) {
        return ticketRepository.findByEventId(eventContext.currentId(), PageRequest.of(0, Math.max(1, limit), Sort.by("id"))).getContent();
    }

    /** True if a ticket with this number was generated for the current event (without loading its image). */
    public boolean exists(String ticketNo) {
        return ticketNo != null && ticketRepository.findTicketInfoByTicketNo(eventContext.currentId(), ticketNo).isPresent();
    }

    /** True if the number belongs to a ticket of another event (e.g. an old QR code scanned at a new event). */
    public boolean existsInOtherEvent(String ticketNo) {
        return ticketNo != null && !exists(ticketNo) && ticketRepository.existsAnywhere(ticketNo);
    }

    public long countTickets() {
        return ticketRepository.countByEventId(eventContext.currentId());
    }

    /**
     * Get ticket count by status without loading data
     */
    public long countTicketsByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(eventContext.currentId(), status);
    }

    public long countTicketsByStatus(Long eventId, TicketStatus status) {
        return ticketRepository.countByStatus(eventId, status);
    }

    /** Id of the event the caller works on; capture it before handing work to a background thread. */
    public Long currentEventId() {
        return eventContext.currentId();
    }
}
