package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserService userService;
    private final TweetService tweetService;
    private final EventService eventService;

    @GetMapping
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "timeline";
    }
}