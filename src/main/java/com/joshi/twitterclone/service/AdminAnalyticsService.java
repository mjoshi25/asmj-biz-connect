package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.events.EventListing;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.repository.events.EventBookingRepository;
import com.joshi.twitterclone.repository.events.EventListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final EventListingRepository eventListingRepository;
    private final EventBookingRepository eventBookingRepository;

    public Map<String, Object> getAnalyticsSummary() {
        Map<String, Object> stats = new HashMap<>();
        List<EventListing> allEvents = eventListingRepository.findAll();
        
        // Exclude 'admin' user from the management list
        List<User> managedUsers = userRepository.findAll().stream()
                .filter(u -> !"admin".equalsIgnoreCase(u.getUsername()))
                .collect(Collectors.toList());

        stats.put("totalUsers", userRepository.count());
        stats.put("totalEvents", eventListingRepository.count());
        stats.put("totalBookings", eventBookingRepository.count());
        stats.put("usersList", managedUsers);
        stats.put("pendingEvents", allEvents.stream().filter(e -> "PENDING".equals(String.valueOf(e.getStatus()))).collect(Collectors.toList()));
        stats.put("approvedEvents", allEvents.stream().filter(e -> "APPROVED".equals(String.valueOf(e.getStatus()))).collect(Collectors.toList()));
        stats.put("rejectedEvents", allEvents.stream().filter(e -> "REJECTED".equals(String.valueOf(e.getStatus()))).collect(Collectors.toList()));

        return stats;
    }
}