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
@Document(collection = "vehicle_listings")
public class VehicleListing {

    @Id
    private String id;

    @Indexed
    private String ownerUsername;
    private String ownerDisplayName;
    private String contactNumber;

    private String make;
    private String modelName;
    private int year;
    private VehicleType vehicleType;
    private String fuelType;
    private String transmission;
    private int seatingCapacity;
    
    private String locationCity;
    private String pickupAddress;
    private BigDecimal dailyRentalRate;
    private BigDecimal securityDeposit;

    private String description;
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING_APPROVAL;
    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}