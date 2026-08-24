package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ThreadDto;
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

    @GetMapping("/{tweetId}/thread")
    public String viewThread(@PathVariable("tweetId") String tweetId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);
        ThreadDto thread = tweetService.getThread(tweetId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("mainTweet", thread.getMainTweet());
        model.addAttribute("replies", thread.getReplies());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(username, 4));

        return "thread";
    }

    @PostMapping("/{tweetId}/reply")
    public String postReply(@PathVariable("tweetId") String tweetId,
                            @RequestParam("content") String content,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Tweet reply = tweetService.createReply(userDetails.getUsername(), tweetId, content);
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweet", reply);

        return "fragments/tweet-feed :: tweet-list";
    }
}