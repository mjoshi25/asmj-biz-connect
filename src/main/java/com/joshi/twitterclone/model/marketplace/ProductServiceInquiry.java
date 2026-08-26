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
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_service_inquiries")
public class ProductServiceInquiry {

    @Id
    private String id;

    @Indexed
    private String listingId;
    private String listingTitle;

    @Indexed
    private String buyerUsername;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerCompanyName;

    @Indexed
    private String vendorUsername;

    private int requestedQuantity;
    private BigDecimal proposedBudget;
    private String projectRequirements;

    @Builder.Default
    private String status = "NEW"; // NEW, IN_REVIEW, QUOTE_SENT, CLOSED

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a"));
    }
}