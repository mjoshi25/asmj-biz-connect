package com.joshi.twitterclone.dto;

import java.util.List;

import com.joshi.twitterclone.model.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationSummaryDto {
    private String conversationId;
    private boolean isGroup;
    private String title;
    private String avatarUrl;
    private String defaultInitial;
    private User otherUser; // null if group
    private List<String> memberUsernames;
    private String lastMessage;
    private String lastSenderName;
    private String lastMessageTimeFormatted;
    private int unreadCount;
}