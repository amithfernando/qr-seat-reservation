package com.qrseatreservation.core.util;

import com.qrseatreservation.core.service.TicketService;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class QrTicketGeneratorTest {

    @Test
    void ticketTextIsDrawnAtExactlyTheConfiguredPosition() {
        BufferedImage base = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
        int textX = 400, textY = 600, fontSize = 40;
        QrTicketGenerator gen = new QrTicketGenerator(base, fontSize, 4, 10, 10, textX, textY, 1,
                24, 0, 0, "#ffffff", "#ffffff");

        BufferedImage out = gen.generateTicketImage("DJ1234", "");
        BufferedImage text = QrTicketGenerator.renderText("DJ1234", fontSize, Color.WHITE);

        int minX = Integer.MAX_VALUE, maxX = -1, minY = Integer.MAX_VALUE, maxY = -1;
        for (int y = 300; y < out.getHeight(); y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                if ((out.getRGB(x, y) & 0xFFFFFF) != 0) {
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                }
            }
        }
        // Text pixels must lie inside the same box the layout editor shows: (textX, textY, w, h)
        assertTrue(minX >= textX && minX <= textX + 10, "left edge at " + minX);
        assertTrue(minY >= textY && minY <= textY + fontSize / 2, "top edge at " + minY);
        assertTrue(maxX < textX + text.getWidth(), "right edge inside box");
        assertTrue(maxY < textY + text.getHeight(), "bottom edge inside box");
    }

    @Test
    void largeFontsAreNotClipped() {
        BufferedImage img = QrTicketGenerator.renderText("DJ1234", 120, Color.BLACK);
        assertTrue(img.getHeight() >= 120);
        assertTrue(img.getWidth() > 300);
    }

    @Test
    void ticketFormatMatching() {
        assertTrue(TicketService.matchesFormat("DJ0041", "DJ", 4));
        assertFalse(TicketService.matchesFormat("T041084", "DJ", 4));
        assertFalse(TicketService.matchesFormat("DJ00411", "DJ", 4));
        assertTrue(TicketService.matchesFormat("000123", "", 6));
        assertTrue(TicketService.matchesFormat("A.B12", "A.B", 2));
        assertFalse(TicketService.matchesFormat("AXB12", "A.B", 2));
    }
}
