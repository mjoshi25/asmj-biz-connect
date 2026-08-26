package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.events.EventBooking;
import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @GetMapping
    public String viewEvents(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(value = "city", required = false) String city,
                             Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<EventListing> events = eventService.getUpcomingEvents(city);
        List<EventBooking> myBookings = eventService.getBookingsForAttendee(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", events);
        model.addAttribute("myBookings", myBookings);
        model.addAttribute("selectedCity", city != null ? city : "");

        return "events";
    }

    @PostMapping("/post")
    public String postEvent(@AuthenticationPrincipal UserDetails userDetails,
                            @ModelAttribute EventListing eventListing,
                            @RequestParam(value = "banner", required = false) MultipartFile banner) {
        eventService.createEvent(userDetails.getUsername(), eventListing, banner);
        return "redirect:/events?posted=true";
    }

    @PostMapping("/book")
    public String bookEvent(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("eventId") String eventId,
                            @RequestParam("fullName") String fullName,
                            @RequestParam("email") String email,
                            @RequestParam("phone") String phone,
                            @RequestParam("tickets") int tickets) {
        eventService.bookTickets(userDetails.getUsername(), eventId, fullName, email, phone, tickets);
        return "redirect:/events?booked=true";
    }

    @GetMapping("/manage")
    public String manageEvents(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<EventListing> organizedEvents = eventService.getEventsOrganizedBy(username);
        List<EventBooking> attendeeBookings = eventService.getBookingsForOrganizer(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("organizedEvents", organizedEvents);
        model.addAttribute("attendeeBookings", attendeeBookings);

        return "event-manage";
    }
}