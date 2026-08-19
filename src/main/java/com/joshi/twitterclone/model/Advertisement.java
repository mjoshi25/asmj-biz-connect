package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "advertisements")
public class Advertisement {

    @Id
    private String id;

    @Indexed
    private String advertiserId;
    private String advertiserUsername;
    private String advertiserDisplayName;

    private String title;
    private String description;
    private String targetUrl;
    private String mediaUrl;
    private String mediaType; // "IMAGE" or "VIDEO"
    private BigDecimal budget;

    // Status: PENDING_APPROVAL, APPROVED, REJECTED, PAUSED, COMPLETED
    @Indexed
    private String status = "PENDING_APPROVAL";
    
    private String adminFeedback;
    private String reviewedByAdminUsername;
    private LocalDateTime reviewedAt;

    private long impressionsCount = 0;
    private long clicksCount = 0;
    private long messageInquiriesCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
}