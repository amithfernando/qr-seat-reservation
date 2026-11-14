package com.qrseatreservation.core.service;


import com.qrseatreservation.core.cache.BaseImageCache;
import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.TicketStatus;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.model.Ticket;
import com.qrseatreservation.core.repository.SeatReservationRepository;
import com.qrseatreservation.core.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SettingService settingService;

    @Mock
    private BaseImageCache baseImageCache;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private EventContext eventContext;

    private TicketService ticketService;

    private final Event event = Event.builder().id(1L).name("E").status(EventStatus.ACTIVE).build();

    @BeforeEach
    void setup() {
        ticketService = new TicketService(ticketRepository, settingService, baseImageCache, seatReservationRepository, eventContext);
    }

    @Test
    void generateTicketNos_createsConfiguredNumberOfTickets_withImages_andAvailableStatus() throws Exception {
        // Arrange: Setting with valid base image and small numbers for fast test
        Setting setting = baseSetting(2, "T-", 5, createPngBytes(200, 120, Color.WHITE));
        when(eventContext.requireEditable()).thenReturn(event);
        when(settingService.getSetting(1L)).thenReturn(setting);
        when(ticketRepository.countByEventId(1L)).thenReturn(0L);
        when(baseImageCache.getBaseImage(setting.getBaseImage())).thenReturn(decodeImage(setting.getBaseImage()));
        // Ensure uniqueness check passes
        when(ticketRepository.existsAnywhere(anyString())).thenReturn(false);

        List<Ticket> saved = new ArrayList<>();
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<Ticket> tickets = invocation.getArgument(0);
            tickets.forEach(saved::add);
            return saved;
        });

        // Act
        ticketService.generateTicketNos();

        // Assert
        verify(ticketRepository, times(1)).saveAll(any());
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(t -> t.getStatus() == TicketStatus.AVAILABLE);
        assertThat(saved).allMatch(t -> t.getEvent() == event);
        assertThat(saved).allMatch(t -> t.getTicketNo() != null && !t.getTicketNo().isBlank());
        // Image bytes were generated
        assertThat(saved.get(0).getData()).isNotNull().isNotEmpty();
        assertThat(saved.get(1).getData()).isNotNull().isNotEmpty();
    }

    @Test
    void generateQrTicketPreview_returnsBufferedImage() throws Exception {
        // Arrange
        Setting setting = baseSetting(1, "X-", 4, createPngBytes(160, 100, Color.WHITE));
        when(settingService.getSetting()).thenReturn(setting);
        when(baseImageCache.getBaseImage(setting.getBaseImage())).thenReturn(decodeImage(setting.getBaseImage()));

        // Act
        BufferedImage img = ticketService.generateQrTicketPreview(
                setting.getFontSize(), setting.getQrSize(), setting.getQrX(), setting.getQrY(),
                setting.getTextX(), setting.getTextY(), setting.getQrBorder(),
                setting.getTableSeatFontSize(), setting.getTableSeatX(), setting.getTableSeatY(),
                "", "X-0001"
        );

        // Assert
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isGreaterThan(0);
        assertThat(img.getHeight()).isGreaterThan(0);
    }

    @Test
    void generateQrTicketPreview_throwsWhenBaseImageCorrupted() {
        // Arrange: baseImage is not a valid image (random bytes), ImageIO.read will return null
        Setting setting = baseSetting(1, "X-", 4, new byte[]{7, 3, 9, 1});
        when(settingService.getSetting()).thenReturn(setting);
        when(baseImageCache.getBaseImage(setting.getBaseImage()))
            .thenThrow(new IllegalArgumentException("Unsupported or corrupted image data"));

        // Act + Assert
        assertThatThrownBy(() ->
                ticketService.generateQrTicketPreview(12, 13, 10, 10, 10, 10, 1, null, null, null, "", "X-0001")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported or corrupted image data");
    }

    @Test
    void getTicketNumber_marksFirstAvailableAsUsed_andReturnsTicketNo() {
        // Arrange
        Ticket available = Ticket.builder()
                .ticketNo("T-12345")
                .status(TicketStatus.AVAILABLE)
                .data(new byte[]{1})
                .build();
        when(ticketRepository.findFirstAvailableTicketForUpdate(1L, TicketStatus.AVAILABLE.name()))
            .thenReturn(available);

        // Act
        String out = ticketService.getTicketNumber(1L);

        // Assert
        assertThat(out).isEqualTo("T-12345");
        assertThat(available.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository, times(1)).save(available);
    }

    @Test
    void getTicketNumber_returnsNull_whenNoAvailableTickets() {
        when(ticketRepository.findFirstAvailableTicketForUpdate(1L, TicketStatus.AVAILABLE.name())).thenReturn(null);

        String out = ticketService.getTicketNumber(1L);

        assertThat(out).isNull();
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void generateTicketNos_skipsNumbersUsedInAnotherEvent_andSetsEvent() throws Exception {
        // 1 digit -> only 10 possible numbers; all but "T-7" are taken in other events
        Setting setting = baseSetting(1, "T-", 1, createPngBytes(200, 120, Color.WHITE));
        when(eventContext.requireEditable()).thenReturn(event);
        when(settingService.getSetting(1L)).thenReturn(setting);
        when(ticketRepository.countByEventId(1L)).thenReturn(0L);
        when(baseImageCache.getBaseImage(setting.getBaseImage())).thenReturn(decodeImage(setting.getBaseImage()));
        when(ticketRepository.existsAnywhere(anyString())).thenAnswer(inv -> !"T-7".equals(inv.getArgument(0)));

        List<Ticket> saved = new ArrayList<>();
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<Ticket> tickets = invocation.getArgument(0);
            tickets.forEach(saved::add);
            return saved;
        });

        ticketService.generateTicketNos();

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getTicketNo()).isEqualTo("T-7");
        assertThat(saved.get(0).getEvent()).isSameAs(event);
        verify(ticketRepository, atLeastOnce()).existsAnywhere(anyString());
    }

    @Test
    void generateTicketNos_doesNothing_whenAlreadyAtMax() throws Exception {
        Setting setting = baseSetting(2, "T-", 5, new byte[]{1});
        when(eventContext.requireEditable()).thenReturn(event);
        when(settingService.getSetting(1L)).thenReturn(setting);
        when(ticketRepository.countByEventId(1L)).thenReturn(2L);

        ticketService.generateTicketNos();

        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void exists_checksTicketWithinCurrentEvent() {
        when(eventContext.currentId()).thenReturn(1L);
        when(ticketRepository.findTicketInfoByTicketNo(1L, "Z-0001")).thenReturn(Optional.empty());

        assertThat(ticketService.exists("Z-0001")).isFalse();
        verify(ticketRepository, times(1)).findTicketInfoByTicketNo(1L, "Z-0001");
    }

    @Test
    void createZipFromImages_writesEachTicketAsEntry_withCorrectNameAndContent() throws Exception {
        Ticket t1 = Ticket.builder().ticketNo("A1").data(new byte[]{1, 2, 3}).build();
        Ticket t2 = Ticket.builder().ticketNo("B2").data(new byte[]{9, 8}).build();

        byte[] zipBytes = ticketService.createZipFromImages(List.of(t1, t2));

        // Read back zip and verify
        Map<String, byte[]> entries = unzip(zipBytes);
        assertThat(entries).containsOnlyKeys("A1.jpg", "B2.jpg");
        assertThat(entries.get("A1.jpg")).containsExactly(1, 2, 3);
        assertThat(entries.get("B2.jpg")).containsExactly(9, 8);
    }

    // Helpers

    private Setting baseSetting(int maxTickets, String prefix, int digits, byte[] baseImage) {
        return Setting.builder()
                .baseImage(baseImage)
                .fontSize(20)
                .qrSize(13)
                .qrBorder(1)
                .qrX(10)
                .qrY(10)
                .textX(10)
                .textY(10)
                .ticketPrefix(prefix)
                .noOfDigits(digits)
                .maxNoOfTickets(maxTickets)
                .build();
    }

    private byte[] createPngBytes(int width, int height, Color color) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private BufferedImage decodeImage(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        if (image == null) {
            throw new IllegalArgumentException("Unsupported or corrupted image data");
        }
        return image;
    }

    private Map<String, byte[]> unzip(byte[] zip) throws IOException {
        Map<String, byte[]> out = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                zis.transferTo(baos);
                out.put(entry.getName(), baos.toByteArray());
                zis.closeEntry();
            }
        }
        return out;
    }

    @Test
    void toJpeg_flattensTransparentImages_soTransparentPngBackgroundsWork() throws Exception {
        java.awt.image.BufferedImage argb = new java.awt.image.BufferedImage(40, 30, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        argb.setRGB(5, 5, 0x80FF0000); // half-transparent red pixel, rest fully transparent

        byte[] jpg = TicketService.toJpeg(argb);

        java.awt.image.BufferedImage back = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(jpg));
        assertThat(back).isNotNull();
        assertThat(back.getWidth()).isEqualTo(40);
        assertThat(back.getColorModel().hasAlpha()).isFalse();
        assertThat(back.getRGB(30, 25) & 0xFFFFFF).isGreaterThan(0xF0F0F0); // transparent area became white
    }
}
