package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ThreadDto;
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

@Controller
@RequestMapping("/tweets")
@RequiredArgsConstructor
public class ThreadController {

    private final TweetService tweetService;
    private final UserService userService;
    private final EventService eventService;

    @GetMapping("/{tweetId}")
    public String viewThread(@PathVariable("tweetId") String tweetId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        User currentUser = username != null ? userService.getUserByUsername(username) : null;

        ThreadDto thread = tweetService.getThread(tweetId, username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("thread", thread);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "timeline";
    }

    @PostMapping("/{tweetId}/reply")
    public String replyToTweet(@PathVariable("tweetId") String tweetId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("content") String content,
                               @RequestParam(value = "image", required = false) MultipartFile image) {
        tweetService.replyToTweet(userDetails.getUsername(), tweetId, content, image);
        return "redirect:/tweets/" + tweetId;
    }

    @PostMapping("/{tweetId}/like")
    @ResponseBody
    public int toggleLike(@PathVariable("tweetId") String tweetId,
                          @AuthenticationPrincipal UserDetails userDetails) {
        Tweet updatedTweet = tweetService.toggleLike(tweetId, userDetails.getUsername());
        return updatedTweet.getLikesCount();
    }
}