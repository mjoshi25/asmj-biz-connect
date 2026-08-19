package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    private boolean isGroup = false;
    private String name; // Name for group conversations
    private String groupAvatarUrl;
    private String createdByUserId;

    @Indexed
    private Set<String> participantIds = new HashSet<>();
    private Set<String> participantUsernames = new HashSet<>();

    private String lastMessageContent;
    private String lastSenderUsername;
    private String lastSenderDisplayName;
    private LocalDateTime lastMessageTime = LocalDateTime.now();

    // Map of userId -> unread count
    private Map<String, Integer> unreadCounts = new HashMap<>();

    public static String buildDirectConversationId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? "dm_" + u1 + "_" + u2 : "dm_" + u2 + "_" + u1;
    }
}