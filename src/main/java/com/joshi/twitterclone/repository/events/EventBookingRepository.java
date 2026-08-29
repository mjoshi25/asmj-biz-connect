package com.joshi.twitterclone.repository.events;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.joshi.twitterclone.model.events.EventBooking;

@Repository
public interface EventBookingRepository extends MongoRepository<EventBooking, String> {
    List<EventBooking> findByEventIdInOrderByBookingDateDesc(List<String> eventIds);
}