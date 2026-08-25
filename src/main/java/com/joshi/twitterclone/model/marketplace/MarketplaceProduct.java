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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "marketplace_products")
public class MarketplaceProduct {

    @Id
    private String id;

    @Indexed
    private String sellerUsername;
    private String sellerDisplayName;
    private String contactNumber;

    private String title;
    private String description;
    private ProductCategory category;
    private BigDecimal unitPrice;
    private int stockQuantity;

    private String shelfAisle;
    private String badgeTag;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private ListingStatus status = ListingStatus.APPROVED;
    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}