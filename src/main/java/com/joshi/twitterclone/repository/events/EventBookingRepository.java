package com.joshi.twitterclone.repository.events;

import com.joshi.twitterclone.model.events.EventBooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventBookingRepository extends MongoRepository<EventBooking, String> {
    List<EventBooking> findByAttendeeUsernameOrderByBookedAtDesc(String attendeeUsername);
    List<EventBooking> findByEventIdOrderByBookedAtDesc(String eventId);
    List<EventBooking> findByOrganizerUsernameOrderByBookedAtDesc(String organizerUsername);
}