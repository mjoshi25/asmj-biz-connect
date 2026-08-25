package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TimelineController {

    private final TweetService tweetService;
    private final UserService userService;

    @GetMapping({"/home", "/timeline"})
    public String home(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(value = "filter", defaultValue = "for-you") String filter,
                       Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;

        List<Tweet> tweets;
        if ("following".equalsIgnoreCase(filter) && currentUser != null && currentUser.getFollowingUsernames() != null && !currentUser.getFollowingUsernames().isEmpty()) {
            tweets = tweetService.getTweetsByAuthors(currentUser.getFollowingUsernames());
        } else if ("following".equalsIgnoreCase(filter)) {
            tweets = List.of();
        } else {
            tweets = tweetService.getAllTweets();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", tweets);
        model.addAttribute("activeFilter", filter.toLowerCase());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(username, 5));

        return "timeline";
    }
}