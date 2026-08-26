package com.joshi.twitterclone.model.products;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
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
@Document(collection = "products_services")
public class ProductServiceItem {

    @Id
    private String id;

    @Indexed
    private String vendorUsername;
    private String vendorDisplayName;
    private String businessName;
    private String contactNumber;

    private String title;
    private ItemCategory category;
    private BigDecimal price;
    private String priceUnit;
    
    private String locationCity;
    private String deliveryTerms;
    private String description;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING_APPROVAL;
    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}