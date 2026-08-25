package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExploreController {

    private final TweetService tweetService;
    private final UserService userService;

    @GetMapping("/explore")
    public String explore(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(value = "q", required = false) String query,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "20") int size,
                          Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;

        List<Tweet> results;
        if (query != null && query.startsWith("#")) {
            results = tweetService.getTweetsByHashtag(query, page, size);
        } else if (query != null && !query.isBlank()) {
            results = tweetService.searchTweets(query, page, size);
        } else {
            results = tweetService.getAllTweets();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", results);
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(username, 4));

        return "explore";
    }
}