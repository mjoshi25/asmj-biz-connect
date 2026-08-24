package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    private String authorAvatarUrl;

    private String content;
    private String imageUrl;
    private String blurHash;
    private String blurDataUrl;
    private MediaStatus mediaStatus = MediaStatus.NONE;

    @Indexed
    private String parentTweetId;

    private int likesCount = 0;
    private int repliesCount = 0;
    private Set<String> likedByUsernames = new HashSet<>();

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();
}