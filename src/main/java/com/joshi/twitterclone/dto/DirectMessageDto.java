package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageDto {
    private String id;
    private String conversationId;
    private String conversationTitle;
    private boolean isGroup;
    private String senderUsername;
    private String senderDisplayName;
    private String senderAvatarUrl;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String originalFileName;
    private String createdAtFormatted;
    private boolean isSelf;
}