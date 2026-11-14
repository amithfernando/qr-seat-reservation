package com.qrseatreservation.core.util;

import io.nayuki.qrcodegen.QrCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class QrTicketGenerator {

    private final BufferedImage baseImage;
    private final int fontSize;
    private final int qrSize;
    private final int qrX;
    private final int qrY;
    private final int textX;
    private final int textY;
    private final int qrBorder;
    private final Integer tableSeatFontSize;
    private final Integer tableSeatX;
    private final Integer tableSeatY;
    private final Color ticketNoColor;
    private final Color tableSeatColor;

    public QrTicketGenerator(BufferedImage baseImage, int fontSize, int qrSize, int qrX, int qrY, int textX, int textY, int qrBorder,
                             Integer tableSeatFontSize, Integer tableSeatX, Integer tableSeatY) {
        this(baseImage, fontSize, qrSize, qrX, qrY, textX, textY, qrBorder,
                tableSeatFontSize, tableSeatX, tableSeatY, null, null);
    }

    public QrTicketGenerator(BufferedImage baseImage, int fontSize, int qrSize, int qrX, int qrY, int textX, int textY, int qrBorder,
                             Integer tableSeatFontSize, Integer tableSeatX, Integer tableSeatY,
                             String ticketNoColorHex, String tableSeatColorHex) {
        this.baseImage = baseImage;
        this.fontSize = fontSize;
        this.qrSize = qrSize;
        this.qrX = qrX;
        this.qrY = qrY;
        this.textX = textX;
        this.textY = textY;
        this.qrBorder = qrBorder;
        this.tableSeatFontSize = tableSeatFontSize;
        this.tableSeatX = tableSeatX;
        this.tableSeatY = tableSeatY;
        this.ticketNoColor = parseColor(ticketNoColorHex, Color.BLACK);
        this.tableSeatColor = parseColor(tableSeatColorHex, Color.BLACK);
    }

    private static Color parseColor(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Each call creates a fresh canvas copy so this generator can be reused without per-call cloning by callers. */
    public BufferedImage generateTicketImage(String ticketNo, String tableSeatText) {
        try {
            // Clone internally — callers must NOT clone before passing baseImage
            int type = baseImage.getType() == BufferedImage.TYPE_CUSTOM
                    ? BufferedImage.TYPE_INT_RGB : baseImage.getType();
            BufferedImage canvas = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), type);
            Graphics2D gc = canvas.createGraphics();
            gc.drawImage(baseImage, 0, 0, null);
            gc.dispose();

            QrCode qr0 = QrCode.encodeText(ticketNo, QrCode.Ecc.MEDIUM);
            BufferedImage img = toImage(qr0, qrSize, qrBorder);

            Graphics g = canvas.getGraphics();
            g.drawImage(img, qrX, qrY, null);
            g.drawImage(renderText(ticketNo, fontSize, ticketNoColor), textX, textY, null);

            if (tableSeatText != null && !tableSeatText.isBlank()
                    && tableSeatFontSize != null && tableSeatFontSize > 0
                    && tableSeatX != null && tableSeatY != null) {
                g.drawImage(renderText(tableSeatText, tableSeatFontSize, tableSeatColor), tableSeatX, tableSeatY, null);
            }

            g.dispose();
            return canvas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Font used for all ticket text. The layout editor previews text with this exact renderer. */
    public static Font ticketFont(int size) {
        return new Font("Arial", Font.BOLD, size);
    }

    /**
     * Renders text into a tightly-sized transparent image whose top-left corner is the
     * text's anchor point, so drawing it at (x, y) places the text exactly at (x, y).
     */
    public static BufferedImage renderText(String text, int fontSize, Color color) {
        String t = text == null ? "" : text;
        Font font = ticketFont(Math.max(1, fontSize));
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        pg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        pg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = pg.getFontMetrics(font);
        int width = Math.max(1, fm.stringWidth(t) + 2);
        int height = Math.max(1, fm.getAscent() + fm.getDescent());
        pg.dispose();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setFont(font);
        g2d.setColor(color != null ? color : Color.BLACK);
        g2d.drawString(t, 0, fm.getAscent());
        g2d.dispose();
        return img;
    }

    public static BufferedImage renderText(String text, int fontSize, String colorHex) {
        return renderText(text, fontSize, parseColor(colorHex, Color.BLACK));
    }

    /** PNG bytes of {@link #renderText(String, int, String)}, used by the layout editor preview. */
    public static byte[] renderTextPng(String text, int fontSize, String colorHex) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(renderText(text, fontSize, colorHex), "png", baos);
            return baos.toByteArray();
        }
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0)
            throw new IllegalArgumentException("Value out of range");
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
            throw new IllegalArgumentException("Scale or border too large");

        BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, color ? darkColor : lightColor);
            }
        }
        return result;
    }

    private static BufferedImage toImage(QrCode qr, int scale, int border) {
        return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
    }

    /** Generates a QR-only PNG image as bytes, usable for previews. */
    public static byte[] generateQrImageBytes(String text, int scale, int border) throws IOException {
        QrCode qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM);
        BufferedImage img = toImage(qr, scale, border);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (!ImageIO.write(img, "png", baos))
                throw new IOException("No ImageIO writer for png");
            return baos.toByteArray();
        }
    }

    /** Module count (excluding border) for the QR that encodes the given text at MEDIUM ECC. */
    public static int qrModuleCount(String text) {
        return QrCode.encodeText(text, QrCode.Ecc.MEDIUM).size;
    }

}
