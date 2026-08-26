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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_service_listings")
public class ProductServiceListing {

    @Id
    private String id;

    @Indexed
    private String vendorUsername;
    private String vendorDisplayName;
    private String businessName;
    private String contactEmail;
    private String contactPhone;

    private String title;
    private ProductServiceType offeringType;
    private PricingModel pricingModel;

    private BigDecimal price;
    private String currency;
    private String locationCity;
    private String deliveryTimelineOrDuration;

    private String description;
    private String keyFeaturesOrScope;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
    private String brochurePdfUrl;

    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING_APPROVAL;
    private String rejectionReason;

    @Builder.Default
    private int inquiryCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}