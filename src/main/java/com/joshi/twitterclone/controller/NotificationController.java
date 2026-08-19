package com.joshi.twitterclone.controller;

import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.model.Notification;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TweetService tweetService;

    @GetMapping("/notifications")
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Slice<Notification> notifications = notificationService.getUserNotifications(currentUser.getId(), page, 20);

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(userDetails.getUsername(), 4));

        return "notifications";
    }

    @PostMapping("/notifications/mark-read")
    public String markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        notificationService.markAllAsRead(currentUser.getId());
        return "redirect:/notifications";
    }

    @GetMapping("/notifications/unread-count")
    public String getUnreadBadge(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long count = notificationService.getUnreadCount(currentUser.getId());
        model.addAttribute("unreadCount", count);

        return "fragments/notification-badge :: badge";
    }
}