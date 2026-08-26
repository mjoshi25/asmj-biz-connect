package com.joshi.twitterclone.model.events;

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
@Document(collection = "event_bookings")
public class EventBooking {

    @Id
    private String id;

    @Indexed
    private String eventId;
    private String eventTitle;
    private String organizerUsername;

    @Indexed
    private String attendeeUsername;
    private String attendeeFullName;
    private String attendeeEmail;
    private String attendeePhone;

    private int numberOfTickets;
    private BigDecimal totalAmount;
    private String bookingReference;

    @Builder.Default
    private EventBookingStatus status = EventBookingStatus.CONFIRMED;

    @Builder.Default
    private LocalDateTime bookedAt = LocalDateTime.now();

    public String getFormattedBookedAt() {
        if (bookedAt == null) return "";
        return bookedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a"));
    }
}