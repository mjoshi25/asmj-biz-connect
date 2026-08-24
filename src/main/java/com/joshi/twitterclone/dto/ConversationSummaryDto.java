package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDto {
    private String conversationId;
    private String title;
    private String lastMessage;
    private String lastSenderName;
    private String lastMessageTimeFormatted;
    private String defaultInitial;
    private boolean isGroup;
    private int unreadCount;
}