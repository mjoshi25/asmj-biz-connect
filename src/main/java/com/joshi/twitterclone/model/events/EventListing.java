package com.joshi.twitterclone.model.events;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event_listings")
public class EventListing {

    @Id
    private String id;

    @Indexed
    private String organizerUsername;
    private String organizerDisplayName;
    private String organizationName;

    private String title;
    private EventType eventType;
    private EventFormat eventFormat;
    
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String venueLocation;
    private String city;
    private String virtualJoinLink;

    private BigDecimal ticketPrice;
    private int totalCapacity;
    @Builder.Default
    private int bookedSeats = 0;

    private String description;
    private String agendaHighlights;
    private String bannerImageUrl;

    @Builder.Default
    private ListingStatus status = ListingStatus.APPROVED;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (eventDate == null) return "";
        return eventDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    public int getAvailableSeats() {
        return Math.max(0, totalCapacity - bookedSeats);
    }

    public boolean isSoldOut() {
        return getAvailableSeats() <= 0;
    }
}