package com.qrseatreservation.core.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;

/**
 * Cache for base image used in ticket generation
 * Uses SoftReference to allow garbage collection under memory pressure
 */
@Component
@Slf4j
public class BaseImageCache {
    
    private SoftReference<BufferedImage> cachedImage;
    private byte[] imageData;
    
    public synchronized BufferedImage getBaseImage(byte[] newImageData) {
        // Check if image data has changed
        if (imageData == null || !java.util.Arrays.equals(imageData, newImageData)) {
            imageData = newImageData;
            cachedImage = null;
        }
        
        // Check if cached image is still available
        BufferedImage image = (cachedImage != null) ? cachedImage.get() : null;
        
        if (image == null) {
            // Load and cache the image
            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
                image = ImageIO.read(bais);
                if (image == null) {
                    throw new IllegalArgumentException("Unsupported or corrupted image data");
                }
                cachedImage = new SoftReference<>(image);
                log.debug("Base image loaded and cached");
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to decode image", e);
            }
        }
        
        return image;
    }
    
    public synchronized void clear() {
        cachedImage = null;
        imageData = null;
        log.debug("Base image cache cleared");
    }
}
