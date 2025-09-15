package com.amithfernando.qrseatreservation.core.util;

import io.nayuki.qrcodegen.QrCode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class QrTicketGenerator {

    private final BufferedImage baseImage; //"imgs/CN2025.jpeg"
    private final int fontSize; //28
    private final int qrX; //950
    private final int qrY; //820
    private final int textX; //950
    private final int textY; //1120

    public QrTicketGenerator(BufferedImage baseImage, int fontSize, int qrX, int qrY, int textX, int textY) {
        this.baseImage = baseImage;
        this.fontSize = fontSize;
        this.qrX = qrX;
        this.qrY = qrY;
        this.textX = textX;
        this.textY = textY;
    }

    public  BufferedImage generateTicketImage(String ticketNo){
        try
        {
            BufferedImage text = convertTextToGraphic(ticketNo,new Font("Arial", Font.PLAIN, fontSize));
            QrCode qr0 = QrCode.encodeText(ticketNo, QrCode.Ecc.MEDIUM);
            BufferedImage img = toImage(qr0, 13, 1);

            Graphics g = baseImage.getGraphics();
            g.drawImage(img, qrX, qrY, null);
            g.drawImage(text,textX,textY,null);

           g.dispose();
           return baseImage;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public  static BufferedImage convertTextToGraphic(String text, Font font) {

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = 300;
        int height = 50;
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,width,height);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, width/4, fm.getAscent());
        g2d.dispose();
        return img;
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

}
