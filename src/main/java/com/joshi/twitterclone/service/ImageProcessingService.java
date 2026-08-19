package com.joshi.twitterclone.service;

import com.luciad.imageio.webp.WebPWriteParam;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final PlaceholderService placeholderService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Data
    @Builder
    public static class ProcessedImageResult {
        private String imageUrl;
        private String blurHash;
        private String blurDataUrl;
    }

    public ProcessedImageResult processAndSaveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path root = Paths.get(uploadDir);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            BufferedImage processedImage = Thumbnails.of(file.getInputStream())
                    .size(1920, 1080)
                    .useExifOrientation(true)
                    .asBufferedImage();

            String blurHash = placeholderService.generateBlurHash(processedImage);
            String blurDataUrl = placeholderService.generateBase64Placeholder(processedImage);

            String uniqueFileName = UUID.randomUUID() + ".webp";
            File outputFile = root.resolve(uniqueFileName).toFile();
            writeWebpImage(processedImage, outputFile, 0.80f);

            return ProcessedImageResult.builder()
                    .imageUrl("/uploads/" + uniqueFileName)
                    .blurHash(blurHash)
                    .blurDataUrl(blurDataUrl)
                    .build();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image", e);
        }
    }

    public void writeWebpImage(BufferedImage image, File outputFile, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No WebP ImageWriter found. Ensure com.github.gotson:webp-imageio is installed in pom.xml.");
        }

        ImageWriter writer = writers.next();
        
        // Configure WebP Compression parameters
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
        writeParam.setCompressionQuality(quality);

        try (FileImageOutputStream output = new FileImageOutputStream(outputFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }
}