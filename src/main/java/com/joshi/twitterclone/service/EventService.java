package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.events.EventBooking;
import com.joshi.twitterclone.model.events.EventBookingStatus;
import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.repository.events.EventBookingRepository;
import com.joshi.twitterclone.repository.events.EventListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventListingRepository eventListingRepository;
    private final EventBookingRepository eventBookingRepository;
    private final UserService userService;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    public EventListing createEvent(String username, EventListing event, MultipartFile banner) {
        User user = userService.getUserByUsername(username);

        event.setOrganizerUsername(user.getUsername());
        event.setOrganizerDisplayName(user.getDisplayName());
        event.setStatus(ListingStatus.APPROVED);
        event.setBookedSeats(0);
        event.setCreatedAt(LocalDateTime.now());

        if (banner != null && !banner.isEmpty()) {
            event.setBannerImageUrl(fileStorageService.saveImageOptimized(banner));
        }

        return eventListingRepository.save(event);
    }

    public List<EventListing> getUpcomingEvents(String city) {
        LocalDate today = LocalDate.now();
        if (city != null && !city.isBlank()) {
            return eventListingRepository.findByStatusAndCityIgnoreCaseAndEventDateGreaterThanEqualOrderByEventDateAsc(ListingStatus.APPROVED, city.trim(), today);
        }
        return eventListingRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(ListingStatus.APPROVED, today);
    }

    public List<EventListing> getTopUpcomingEvents(int limit) {
        List<EventListing> events = eventListingRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(ListingStatus.APPROVED, LocalDate.now());
        return events.stream().limit(limit).toList();
    }

    public List<EventListing> getEventsOrganizedBy(String username) {
        return eventListingRepository.findByOrganizerUsernameOrderByCreatedAtDesc(username);
    }

    public EventBooking bookTickets(String attendeeUsername, String eventId, String fullName, String email, String phone, int ticketCount) {
        EventListing event = eventListingRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getAvailableSeats() < ticketCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough seats available for this event.");
        }

        BigDecimal unitPrice = event.getTicketPrice() != null ? event.getTicketPrice() : BigDecimal.ZERO;
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(ticketCount));

        String refCode = "ASMJ-EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EventBooking booking = EventBooking.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .organizerUsername(event.getOrganizerUsername())
                .attendeeUsername(attendeeUsername)
                .attendeeFullName(fullName)
                .attendeeEmail(email)
                .attendeePhone(phone)
                .numberOfTickets(ticketCount)
                .totalAmount(totalAmount)
                .bookingReference(refCode)
                .status(EventBookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build();

        EventBooking savedBooking = eventBookingRepository.save(booking);

        event.setBookedSeats(event.getBookedSeats() + ticketCount);
        eventListingRepository.save(event);

        // Notify organizer via integrated messaging
        Conversation convo = directMessageService.getOrCreateDirectConversation(attendeeUsername, event.getOrganizerUsername());
        String notification = String.format("Hi! I just booked %d ticket(s) for '%s' (Ref: %s). Looking forward to the event!",
                ticketCount, event.getTitle(), refCode);
        directMessageService.sendMessage(attendeeUsername, convo.getId(), notification, null);

        return savedBooking;
    }

    public List<EventBooking> getBookingsForAttendee(String attendeeUsername) {
        return eventBookingRepository.findByAttendeeUsernameOrderByBookedAtDesc(attendeeUsername);
    }

    public List<EventBooking> getBookingsForOrganizer(String organizerUsername) {
        return eventBookingRepository.findByOrganizerUsernameOrderByBookedAtDesc(organizerUsername);
    }
}