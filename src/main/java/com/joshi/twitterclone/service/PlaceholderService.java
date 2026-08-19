package com.joshi.twitterclone.service;

import io.trbl.blurhash.BlurHash;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class PlaceholderService {

    // Generate standard BlurHash string
    public String generateBlurHash(BufferedImage image) {
        if (image == null) return null;
        try {
            BufferedImage small = Thumbnails.of(image)
                    .size(64, 64)
                    .asBufferedImage();
            return BlurHash.encode(small, 4, 3);
        } catch (Exception e) {
            return null;
        }
    }

    // Generate ultra-compact Base64 Data URL (~200 bytes)
    public String generateBase64Placeholder(BufferedImage image) {
        if (image == null) return null;
        try {
            // Resize to a tiny 16x16 thumbnail as BufferedImage in memory
            BufferedImage tiny = Thumbnails.of(image)
                    .size(16, 16)
                    .asBufferedImage();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Encode as JPEG into the stream (native ImageIO support, no Thumbnailator format error)
            ImageIO.write(tiny, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}