package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
@CompoundIndex(name = "recipient_created_idx", def = "{'recipientId': 1, 'createdAt': -1}")
public class Notification {
    @Id
    private String id;

    @Indexed
    private String recipientId;

    private String actorId;
    private String actorUsername;
    private String actorDisplayName;

    private NotificationType type;
    private String targetTweetId;
    private String snippet;

    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType {
        MENTION, REPLY, LIKE, FOLLOW
    }
}