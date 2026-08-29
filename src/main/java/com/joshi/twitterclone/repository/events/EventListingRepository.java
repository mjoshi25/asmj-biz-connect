package com.joshi.twitterclone.repository.events;

import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventListingRepository extends MongoRepository<EventListing, String> {
    List<EventListing> findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(ListingStatus status, LocalDate date);
    List<EventListing> findByOrganizerUsernameOrderByEventDateAsc(String organizerUsername);
    
    // Added for Admin Moderation Queue
    List<EventListing> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    
    List<EventListing> findByStatus(ListingStatus status);
}