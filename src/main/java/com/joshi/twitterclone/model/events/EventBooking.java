package com.joshi.twitterclone.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "event_bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBooking {

    @Id
    private String id;
    private String eventId;
    private String eventTitle;
    private String userId;
    private int numberOfTickets;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDateTime bookingDate; // Matches repository query property
}