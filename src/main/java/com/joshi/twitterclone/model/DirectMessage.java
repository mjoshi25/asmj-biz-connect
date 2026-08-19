package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "direct_messages")
@CompoundIndex(name = "convo_created_idx", def = "{'conversationId': 1, 'createdAt': 1}")
public class DirectMessage {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private String senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderAvatarUrl;

    private String content;

    // Media & File Attachment Support
    private String mediaUrl;
    private String mediaType; // "IMAGE" or "FILE"
    private String originalFileName;

    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}