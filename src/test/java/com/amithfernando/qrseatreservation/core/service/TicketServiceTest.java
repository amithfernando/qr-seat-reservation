package com.amithfernando.qrseatreservation.core.service;


import com.amithfernando.qrseatreservation.core.enums.TicketStatus;
import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.model.Ticket;
import com.amithfernando.qrseatreservation.core.repsitory.TicketRepository;
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

    private TicketService ticketService;

    @BeforeEach
    void setup() {
        ticketService = new TicketService(ticketRepository, settingService);
    }

    @Test
    void generateTicketNos_createsConfiguredNumberOfTickets_withImages_andAvailableStatus() throws Exception {
        // Arrange: Setting with valid base image and small numbers for fast test
        Setting setting = baseSetting(2, "T-", 5, createPngBytes(200, 120, Color.WHITE));
        when(settingService.getSetting()).thenReturn(setting);
        // Ensure uniqueness check passes
        when(ticketRepository.findByTicketNo(anyString())).thenReturn(null);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // Act
        ticketService.generateTicketNos();

        // Assert
        verify(ticketRepository, times(2)).save(ticketCaptor.capture());
        List<Ticket> saved = ticketCaptor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(t -> t.getStatus() == TicketStatus.AVAILABLE);
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

        // Act
        BufferedImage img = ticketService.generateQrTicketPreview(
                setting.getFontSize(), setting.getQrX(), setting.getQrY(),
                setting.getTextX(), setting.getTextY(), "X-0001"
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

        // Act + Assert
        assertThatThrownBy(() ->
                ticketService.generateQrTicketPreview(12, 10, 10, 10, 10, "X-0001")
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
        when(ticketRepository.findTcicketByStatus(TicketStatus.AVAILABLE))
                .thenReturn(List.of(available));

        // Act
        String out = ticketService.getTicketNumber();

        // Assert
        assertThat(out).isEqualTo("T-12345");
        assertThat(available.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository, times(1)).save(available);
    }

    @Test
    void getTicketNumber_returnsNull_whenNoAvailableTickets() {
        when(ticketRepository.findTcicketByStatus(TicketStatus.AVAILABLE)).thenReturn(List.of());

        String out = ticketService.getTicketNumber();

        assertThat(out).isNull();
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void findAllTickets_delegatesToRepository() {
        List<Ticket> data = List.of(Ticket.builder().ticketNo("A").build());
        when(ticketRepository.findAll()).thenReturn(data);

        List<Ticket> out = ticketService.findAllTickets();

        assertThat(out).isSameAs(data);
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void findByTicketNo_delegatesToRepository() {
        Ticket t = Ticket.builder().ticketNo("Z-0001").build();
        when(ticketRepository.findByTicketNo("Z-0001")).thenReturn(t);

        Ticket out = ticketService.findByTicketNo("Z-0001");

        assertThat(out).isSameAs(t);
        verify(ticketRepository, times(1)).findByTicketNo("Z-0001");
    }

    @Test
    void createZipFromImages_writesEachTicketAsEntry_withCorrectNameAndContent() throws Exception {
        Ticket t1 = Ticket.builder().ticketNo("A1").data(new byte[]{1, 2, 3}).build();
        Ticket t2 = Ticket.builder().ticketNo("B2").data(new byte[]{9, 8}).build();

        byte[] zipBytes = ticketService.createZipFromImages(List.of(t1, t2));

        // Read back zip and verify
        Map<String, byte[]> entries = unzip(zipBytes);
        assertThat(entries).containsOnlyKeys("A1.png", "B2.png");
        assertThat(entries.get("A1.png")).containsExactly(1, 2, 3);
        assertThat(entries.get("B2.png")).containsExactly(9, 8);
    }

    // Helpers

    private Setting baseSetting(int maxTickets, String prefix, int digits, byte[] baseImage) {
        return Setting.builder()
                .baseImage(baseImage)
                .fontSize(20)
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
}
