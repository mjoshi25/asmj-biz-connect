package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        String username = userDetails != null ? userDetails.getUsername() : "";
        User currentUser = userService.getUserByUsername(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", eventService.getUpcomingEvents(city));
        model.addAttribute("myBookings", eventService.getUserBookings(username));
        model.addAttribute("selectedCity", city != null ? city : "");

        return "events";
    }

    @PostMapping("/book")
    public String bookEvent(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("eventId") String eventId,
                            @RequestParam("numberOfTickets") int numberOfTickets,
                            @RequestParam("attendeeFullName") String fullName,
                            @RequestParam("attendeeEmail") String email,
                            @RequestParam("attendeePhone") String phone) {
        eventService.bookEvent(userDetails.getUsername(), eventId, numberOfTickets, fullName, email, phone);
        return "redirect:/events?booked=true";
    }

    @GetMapping("/manage")
    public String viewOrganizerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myEvents", eventService.getOrganizerEvents(username));
        model.addAttribute("attendeeBookings", eventService.getEventBookingsForOrganizer(username));

        return "events-manage";
    }

    @PostMapping("/post")
    public String postEvent(@AuthenticationPrincipal UserDetails userDetails,
                            @ModelAttribute EventListing event) {
        eventService.createEvent(userDetails.getUsername(), event);
        return "redirect:/events/manage?posted=true";
    }

    @PostMapping("/admin/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateEvent(@PathVariable("id") String id,
                                @RequestParam("approve") boolean approve,
                                @RequestParam(value = "rejectionReason", required = false) String reason) {
        eventService.moderateEvent(id, approve, reason);
        return "redirect:/admin/analytics";
    }
    
    @PostMapping("/admin/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public String revokeEventApproval(@PathVariable("id") String id,
                                      @RequestParam(value = "rejectionReason", required = false, defaultValue = "Administrative policy review") String reason) {
        eventService.revokeEventApproval(id, reason);
        return "redirect:/admin/analytics";
    }
}