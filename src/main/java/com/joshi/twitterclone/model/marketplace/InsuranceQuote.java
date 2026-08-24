package com.joshi.twitterclone.model.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "insurance_quotes")
public class InsuranceQuote {

    @Id
    private String id;

    @Indexed
    private String adId;
    private String adTitle;
    private String insurerUsername;

    @Indexed
    private String applicantUsername;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;

    private int applicantAge;
    private BigDecimal estimatedValueOrSumInsured;
    private BigDecimal calculatedQuotePremium;

    private String status; // GENERATED, CONTACTED, CLOSED
    private String associatedConversationId;

    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();
}