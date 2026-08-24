package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncImageProcessor {

    private final TweetRepository tweetRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void processTweetImageAsync(String tweetId, String rawImageUrl) {
        try {
            Tweet tweet = tweetRepository.findById(tweetId).orElse(null);
            if (tweet == null) return;

            tweet.setImageUrl(rawImageUrl);
            tweet.setBlurHash(null);
            tweet.setBlurDataUrl(rawImageUrl);
            tweet.setMediaStatus(MediaStatus.COMPLETED);

            tweetRepository.save(tweet);

            // Broadcast WebSocket notification to clients
            messagingTemplate.convertAndSend("/topic/tweet-media", Map.of(
                    "tweetId", tweetId,
                    "imageUrl", rawImageUrl,
                    "blurDataUrl", rawImageUrl,
                    "status", "COMPLETED"
            ));

        } catch (Exception e) {
            log.error("Async image processing failed for tweet [{}]: {}", tweetId, e.getMessage());
            tweetRepository.findById(tweetId).ifPresent(t -> {
                t.setMediaStatus(MediaStatus.FAILED);
                tweetRepository.save(t);
            });
        }
    }
}