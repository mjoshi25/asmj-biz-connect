package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    private String name;
    private boolean isGroup;

    @Indexed
    private Set<String> participantUsernames = new HashSet<>();
    private Set<String> participantIds = new HashSet<>();

    private String lastMessage;
    private String lastSenderName;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}