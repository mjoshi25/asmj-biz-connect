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

    public List<EventListing> getUpcomingEvents(String city) {
        List<EventListing> events = eventListingRepository.findAll().stream()
                .filter(e -> e.getStatus() == ListingStatus.APPROVED)
                .collect(Collectors.toList());

        if (city != null && !city.trim().isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getCity() != null && e.getCity().equalsIgnoreCase(city.trim()))
                    .collect(Collectors.toList());
        }
        return events;
    }

    // Resolves the getTopUpcomingEvents(int) compilation errors across controllers
    public List<EventListing> getTopUpcomingEvents(int limit) {
        return eventListingRepository.findAll().stream()
                .filter(e -> e.getStatus() == ListingStatus.APPROVED)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public EventListing getEventById(String id) {
        return eventListingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    public EventListing createEvent(String username, EventListing event) {
        event.setOrganizerUsername(username);
        event.setStatus(ListingStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now());
        return eventListingRepository.save(event);
    }

    public EventListing updateEvent(String id, String username, EventListing updatedEvent, String newBannerUrl) {
        EventListing existing = getEventById(id);

        if (!existing.getOrganizerUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this event");
        }

        existing.setTitle(updatedEvent.getTitle());
        existing.setDescription(updatedEvent.getDescription());
        existing.setEventType(updatedEvent.getEventType());
        existing.setOrganizationName(updatedEvent.getOrganizationName());
        existing.setCity(updatedEvent.getCity());
        existing.setEventDate(updatedEvent.getEventDate());
        existing.setTicketPrice(updatedEvent.getTicketPrice());

        if (newBannerUrl != null && !newBannerUrl.isEmpty()) {
            existing.setBannerUrl(newBannerUrl);
        } else if (updatedEvent.getBannerUrl() != null && !updatedEvent.getBannerUrl().isEmpty()) {
            existing.setBannerUrl(updatedEvent.getBannerUrl());
        }

        existing.setStatus(ListingStatus.PENDING);
        existing.setRejectionReason(null);

        return eventListingRepository.save(existing);
    }

    public void deleteOrganizerEvent(String id, String username) {
        EventListing event = getEventById(id);
        if (!event.getOrganizerUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this event");
        }
        eventListingRepository.deleteById(id);
    }

    public void moderateEvent(String id, boolean approve, String reason) {
        EventListing event = getEventById(id);
        event.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            event.setRejectionReason(reason != null ? reason : "Moderation policy criteria not met.");
        } else {
            event.setRejectionReason(null);
        }
        eventListingRepository.save(event);
    }

    public void revokeEventApproval(String id, String reason) {
        EventListing event = getEventById(id);
        event.setStatus(ListingStatus.REJECTED);
        event.setRejectionReason(reason != null && !reason.trim().isEmpty() ? reason : "Approval revoked by system administrator.");
        eventListingRepository.save(event);
    }

    public List<EventListing> getOrganizerEvents(String username) {
        return eventListingRepository.findAll().stream()
                .filter(e -> username.equals(e.getOrganizerUsername()))
                .collect(Collectors.toList());
    }

    public List<EventBooking> getUserBookings(String username) {
        return eventBookingRepository.findAll().stream()
                .filter(b -> username.equals(b.getUserId()))
                .collect(Collectors.toList());
    }

    public List<EventBooking> getEventBookingsForOrganizer(String username) {
        List<String> organizerEventIds = getOrganizerEvents(username).stream()
                .map(EventListing::getId)
                .collect(Collectors.toList());

        return eventBookingRepository.findAll().stream()
                .filter(b -> organizerEventIds.contains(b.getEventId()))
                .collect(Collectors.toList());
    }

    public EventBooking bookEvent(String username, String eventId, int numberOfTickets, String fullName, String email, String phone) {
        EventListing event = getEventById(eventId);
        EventBooking booking = EventBooking.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .userId(username)
                .numberOfTickets(numberOfTickets)
                .customerName(fullName)
                .customerEmail(email)
                .customerPhone(phone)
                .bookingDate(LocalDateTime.now())
                .build();
        return eventBookingRepository.save(booking);
    }
}