package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.TweetService;
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
@RequestMapping({"/home", "/timeline"})
@RequiredArgsConstructor
public class TimelineController {

    private final TweetService tweetService;
    private final UserService userService;
    private final EventService eventService;

    @GetMapping
    public String viewTimeline(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;

        List<Tweet> tweets = tweetService.getRecentTweets();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", tweets);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "timeline";
    }

    @PostMapping("/tweets")
    public String createTweet(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("content") String content,
                              @RequestParam(value = "image", required = false) MultipartFile image) {
        tweetService.createTweet(userDetails.getUsername(), content, image);
        return "redirect:/home";
    }
}