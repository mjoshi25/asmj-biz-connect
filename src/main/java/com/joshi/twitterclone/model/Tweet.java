package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "tweets")
public class Tweet {
    @Id
    private String id;

    @Indexed
    private String authorId;
    private String authorUsername;
    private String authorDisplayName;

    @TextIndexed(weight = 2)
    private String content;

    @Indexed
    private Set<String> hashtags = new HashSet<>();

    private String imageUrl;
    private String blurHash;
    private String blurDataUrl;
    private MediaStatus mediaStatus = MediaStatus.NONE;

    private Set<String> likedBy = new HashSet<>();
    private int replyCount = 0;

    @Indexed
    private String parentTweetId;

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MediaStatus {
        NONE, PROCESSING, COMPLETED, FAILED
    }
}