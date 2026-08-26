package com.joshi.twitterclone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String mediaUrl;
    
    @Builder.Default
    private MediaStatus mediaStatus = MediaStatus.NONE;

    @Indexed
    private String parentTweetId;

    @Builder.Default
    private int likesCount = 0;

    @Builder.Default
    private int repliesCount = 0;

    @Builder.Default
    private int retweetsCount = 0;

    @Builder.Default
    private Set<String> likedByUsernames = new HashSet<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Image URL alias methods for AsyncImageProcessor compatibility
    public String getImageUrl() {
        return this.mediaUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mediaUrl = imageUrl;
    }

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a"));
    }
}