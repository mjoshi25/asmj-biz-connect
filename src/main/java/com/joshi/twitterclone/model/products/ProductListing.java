package com.joshi.twitterclone.model.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.joshi.twitterclone.model.marketplace.ListingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "product_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListing {

    @Id
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String location;
    private String ownerUsername;
    private String imageUrl;
    private ListingStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
}