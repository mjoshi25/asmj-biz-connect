package com.joshi.twitterclone.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.model.events.EventBooking;
import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.repository.events.EventBookingRepository;
import com.joshi.twitterclone.repository.events.EventListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventListingRepository eventListingRepository;
    private final EventBookingRepository eventBookingRepository;
    private final NotificationService notificationService;

    public List<EventListing> getAllEvents() {
        return eventListingRepository.findAll();
    }

    public EventListing getEventById(String id) {
        return eventListingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    public List<EventListing> getPendingEvents() {
        return eventListingRepository.findAll().stream()
                .filter(e -> e.getStatus() == ListingStatus.PENDING)
                .collect(Collectors.toList());
    }

    public List<EventListing> getApprovedEvents() {
        return eventListingRepository.findAll().stream()
                .filter(e -> e.getStatus() == ListingStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public List<EventListing> getTopUpcomingEvents(int limit) {
        return getApprovedEvents().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<EventListing> getUpcomingEvents(String username) {
        return getApprovedEvents();
    }

    public List<EventListing> getOrganizerEvents(String username) {
        return eventListingRepository.findAll().stream()
                .filter(e -> username.equals(e.getOrganizerUsername()))
                .collect(Collectors.toList());
    }

    public EventListing createEvent(String username, EventListing event) {
        event.setOrganizerUsername(username);
        event.setStatus(ListingStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now());
        return eventListingRepository.save(event);
    }

    public EventBooking bookEvent(String eventId, String username, int tickets, String attendeeName, String email, String phone) {
        EventListing event = getEventById(eventId);
        
        EventBooking booking = EventBooking.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .userId(username)
                .numberOfTickets(tickets)
                .customerName(attendeeName)
                .customerEmail(email)
                .customerPhone(phone)
                .bookingDate(LocalDateTime.now())
                .build();

        EventBooking savedBooking = eventBookingRepository.save(booking);
        
        notificationService.sendNotification(event.getOrganizerUsername(), "New Event Booking",
                attendeeName + " booked " + tickets + " ticket(s) for '" + event.getTitle() + "'.", "/events/manage");

        return savedBooking;
    }

    public List<EventBooking> getUserBookings(String username) {
        return eventBookingRepository.findAll().stream()
                .filter(b -> username.equals(b.getUserId()))
                .collect(Collectors.toList());
    }

    public List<EventBooking> getEventBookingsForOrganizer(String organizerUsername) {
        List<String> organizerEventIds = getOrganizerEvents(organizerUsername).stream()
                .map(EventListing::getId)
                .toList();

        return eventBookingRepository.findAll().stream()
                .filter(b -> organizerEventIds.contains(b.getEventId()))
                .collect(Collectors.toList());
    }

    public void deleteEvent(String id) {
        eventListingRepository.deleteById(id);
    }

    public void moderateEvent(String eventId, boolean approve, String rejectionReason) {
        EventListing event = getEventById(eventId);

        if (approve) {
            event.setStatus(ListingStatus.APPROVED);
            event.setRejectionReason(null);
            notificationService.sendNotification(event.getOrganizerUsername(), "Event Approved", 
                    "Your event '" + event.getTitle() + "' has been approved and published.", "/events");
        } else {
            event.setStatus(ListingStatus.REJECTED);
            event.setRejectionReason(rejectionReason);
            notificationService.sendNotification(event.getOrganizerUsername(), "Event Rejected", 
                    "Your event '" + event.getTitle() + "' was rejected. Reason: " + rejectionReason, "/events/manage");
        }
        eventListingRepository.save(event);
    }

    public void revokeEventApproval(String eventId, String rejectionReason) {
        EventListing event = getEventById(eventId);

        event.setStatus(ListingStatus.REJECTED);
        event.setRejectionReason(rejectionReason);
        eventListingRepository.save(event);

        notificationService.sendNotification(event.getOrganizerUsername(), "Event Approval Revoked", 
                "Your event '" + event.getTitle() + "' approval was revoked. Reason: " + rejectionReason, "/events/manage");
    }
    
    public List<EventListing> getRejectedEvents() {
        return eventListingRepository.findAll().stream()
                .filter(e -> e.getStatus() == ListingStatus.REJECTED)
                .collect(Collectors.toList());
    }
}

