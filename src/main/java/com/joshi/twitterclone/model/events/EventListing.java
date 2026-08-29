package com.joshi.twitterclone.model.events;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.joshi.twitterclone.model.marketplace.ListingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "event_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListing {

    @Id
    private String id;
    private String title;
    private String description;
    private EventType eventType;
    private EventFormat eventFormat;
    private String organizationName;
    private String organizerUsername;
    private String organizerDisplayName;
    private String city;
    private LocalDate eventDate;
    private LocalTime startTime; // Added to resolve DataSeeder builder errors
    private LocalTime endTime; // Added to resolve DataSeeder builder errors
    private BigDecimal ticketPrice;
    private String bannerUrl;
    private String venueLocation;
    private int totalCapacity;
    private int bookedSeats;
    private String bannerImageUrl;
    private ListingStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    
    public String getFormattedDate() {
        if (eventDate != null) {
            return eventDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }
        return "";
    }
}