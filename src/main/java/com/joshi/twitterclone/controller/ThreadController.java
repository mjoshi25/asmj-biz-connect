package com.joshi.twitterclone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.dto.ThreadDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ThreadController {

    private final TweetService tweetService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/tweets/{tweetId}/thread")
    public String viewThread(@PathVariable("tweetId") String tweetId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ThreadDto thread = tweetService.getThread(tweetId);

        model.addAttribute("thread", thread);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(userDetails.getUsername(), 4));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));

        return "thread";
    }

    @PostMapping("/tweets/{tweetId}/reply")
    public String postReply(@PathVariable("tweetId") String tweetId,
                            @RequestParam("content") String content,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        String trimmed = content != null ? content.trim() : "";
        if (trimmed.isEmpty() || trimmed.length() > 280) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reply must be 1-280 chars");
        }

        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet reply = tweetService.createReply(tweetId, userDetails.getUsername(), trimmed);

        model.addAttribute("tweet", reply);
        model.addAttribute("currentUserId", currentUser.getId());

        return "fragments/tweet-components :: tweet-card";
    }
}