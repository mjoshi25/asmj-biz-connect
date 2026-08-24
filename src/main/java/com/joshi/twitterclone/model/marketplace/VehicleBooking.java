package com.joshi.twitterclone.model.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicle_bookings")
public class VehicleBooking {

    @Id
    private String id;

    @Indexed
    private String listingId;
    private String vehicleSummary;

    @Indexed
    private String renterUsername;
    private String ownerUsername;

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private BigDecimal totalAmount;

    private String customerPhone;
    private String drivingLicenseNumber;

    @Builder.Default
    private String bookingStatus = "CONFIRMED"; // PENDING, CONFIRMED, COMPLETED, CANCELLED

    @Builder.Default
    private LocalDateTime bookedAt = LocalDateTime.now();
}