package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPushDto {
    private String id;
    private String type;
    private String actorUsername;
    private String actorDisplayName;
    private String targetTweetId;
    private String snippet;
    private long unreadCount;
    private String createdAt;
}