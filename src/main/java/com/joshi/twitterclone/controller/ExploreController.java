package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.SearchService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/explore")
@RequiredArgsConstructor
public class ExploreController {

    private final SearchService searchService;
    private final UserService userService;
    private final TweetService tweetService;
    private final EventService eventService;

    @GetMapping
    public String search(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(value = "q", required = false) String query,
                         Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;

        List<Tweet> searchResults = (query != null && !query.isBlank())
                ? searchService.searchTweets(query)
                : tweetService.getRecentTweets();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", searchResults);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "explore";
    }
}