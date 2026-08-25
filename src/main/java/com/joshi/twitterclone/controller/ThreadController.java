package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ThreadViewDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tweets")
@RequiredArgsConstructor
public class ThreadController {

    private final TweetService tweetService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String viewThread(@PathVariable("id") String id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;
        ThreadViewDto thread = tweetService.getThread(id);

        model.addAttribute("thread", thread);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(username, 4));

        return "thread";
    }

    @PostMapping("/{id}/reply")
    public String replyToTweet(@PathVariable("id") String id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("content") String content) {
        if (userDetails != null) {
            tweetService.createReply(userDetails.getUsername(), id, content);
            return "redirect:/tweets/" + id;
        }
        return "redirect:/login";
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable("id") String id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        Tweet tweet = tweetService.toggleLike(id, username);
        model.addAttribute("tweet", tweet);
        model.addAttribute("currentUser", username != null ? userService.getUserByUsername(username) : null);
        return "fragments/tweet-feed :: tweet-list";
    }
}