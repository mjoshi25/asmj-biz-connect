package com.joshi.twitterclone.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.repository.TweetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncImageProcessor {

    private final FileStorageService fileStorageService;
    private final TweetRepository tweetRepository;

    @Async
    public CompletableFuture<Void> processAndAttachImage(String tweetId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("Processing media for tweet ID: {}", tweetId);
            String imageUrl = fileStorageService.saveImageOptimized(file);

            tweetRepository.findById(tweetId).ifPresent(tweet -> {
                tweet.setImageUrl(imageUrl);
                tweet.setMediaStatus(MediaStatus.READY);
                tweetRepository.save(tweet);
                log.info("Media processing completed for tweet ID: {}", tweetId);
            });
        } catch (Exception e) {
            log.error("Failed to process media for tweet ID {}: {}", tweetId, e.getMessage());
            tweetRepository.findById(tweetId).ifPresent(tweet -> {
                tweet.setMediaStatus(MediaStatus.FAILED);
                tweetRepository.save(tweet);
            });
        }

        return CompletableFuture.completedFuture(null);
    }
}