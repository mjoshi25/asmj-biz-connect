package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final TweetService tweetService;

    @GetMapping("/")
    public String rootRedirect(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/home";
        }
        return "index";
    }

    @GetMapping("/home")
    public String viewHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", tweetService.getRecentTweets());
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));
        model.addAttribute("unreadNotificationCount", notificationService.getUnreadCount(username));

        return "timeline";
    }
}