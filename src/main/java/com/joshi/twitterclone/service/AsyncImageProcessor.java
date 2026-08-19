package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.TweetMediaUpdateDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncImageProcessor {

    private final TweetRepository tweetRepository;
    private final PlaceholderService placeholderService;
    private final ImageProcessingService imageProcessingService;
    private final TweetBroadcastService tweetBroadcastService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Async("imageTaskExecutor")
    public void processTweetImageAsync(String tweetId, byte[] imageBytes) {
        try {
            Path root = Paths.get(uploadDir);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // 1. Process thumbnail in memory from raw bytes
            BufferedImage processedImage = Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(1920, 1080)
                    .useExifOrientation(true)
                    .asBufferedImage();

            // 2. Generate Placeholders
            String blurHash = placeholderService.generateBlurHash(processedImage);
            String blurDataUrl = placeholderService.generateBase64Placeholder(processedImage);

            // 3. Compress & Save full image as WebP
            String uniqueFileName = UUID.randomUUID() + ".webp";
            File outputFile = root.resolve(uniqueFileName).toFile();
            imageProcessingService.writeWebpImage(processedImage, outputFile, 0.80f);
            String finalImageUrl = "/uploads/" + uniqueFileName;

            // 4. Update MongoDB Document
            Tweet tweet = tweetRepository.findById(tweetId).orElse(null);
            if (tweet != null) {
                tweet.setImageUrl(finalImageUrl);
                tweet.setBlurHash(blurHash);
                tweet.setBlurDataUrl(blurDataUrl);
                tweet.setMediaStatus(Tweet.MediaStatus.COMPLETED);
                tweetRepository.save(tweet);

                // 5. Broadcast real-time push
                TweetMediaUpdateDto payload = new TweetMediaUpdateDto(
                        tweetId, finalImageUrl, blurHash, blurDataUrl, "COMPLETED"
                );
                messagingTemplate.convertAndSend("/topic/tweet-media", payload);

                // 6. HTMX WebSocket swap
                tweetBroadcastService.broadcastMediaReady(tweetId, finalImageUrl, blurDataUrl);
            }
        } catch (Exception e) {
            log.error("Async image processing failed for tweet: {}", tweetId, e);
            tweetRepository.findById(tweetId).ifPresent(t -> {
                t.setMediaStatus(Tweet.MediaStatus.FAILED);
                tweetRepository.save(t);
            });
        }
    }
}