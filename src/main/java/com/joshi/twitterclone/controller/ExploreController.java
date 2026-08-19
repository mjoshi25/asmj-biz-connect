package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ExploreController {

    private final TweetService tweetService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/hashtag/{tag}")
    public String viewHashtag(@PathVariable("tag") String tag,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Slice<Tweet> tweets = tweetService.getTweetsByHashtag(tag, 0, 20);

        model.addAttribute("pageTitle", "#" + tag);
        model.addAttribute("tweets", tweets);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getId()));

        return "explore";
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String query,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Slice<Tweet> results = tweetService.searchTweets(query, 0, 20);

        model.addAttribute("pageTitle", "Search: " + query);
        model.addAttribute("tweets", results);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getId()));

        return "explore";
    }
}