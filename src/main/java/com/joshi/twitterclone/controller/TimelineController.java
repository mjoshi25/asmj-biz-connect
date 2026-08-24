package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping({"/home", "/timeline"})
@RequiredArgsConstructor
public class TimelineController {

    private final TweetService tweetService;
    private final UserService userService;

    @GetMapping
    public String viewTimeline(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "20") int size,
                               Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);
        Page<Tweet> tweetsPage = tweetService.getTimelineSlice(username, page, size);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", tweetsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", tweetsPage.hasNext());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(username, 4));

        return "timeline";
    }

    @GetMapping("/feed")
    public String getTimelineFeedSlice(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "20") int size,
                                       Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);
        Page<Tweet> tweetsPage = tweetService.getTimelineSlice(username, page, size);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweets", tweetsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", tweetsPage.hasNext());

        return "fragments/tweet-feed :: tweet-list";
    }

    @PostMapping("/tweets")
    public String createTweet(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("content") String content,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Model model) {
        Tweet newTweet = tweetService.createTweetWithAsyncImage(userDetails.getUsername(), content, image);
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweet", newTweet);

        return "fragments/tweet-feed :: tweet-list";
    }

    @PostMapping("/tweets/{tweetId}/like")
    public String toggleLike(@PathVariable("tweetId") String tweetId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Tweet updatedTweet = tweetService.toggleLike(tweetId, userDetails.getUsername());
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tweet", updatedTweet);

        return "fragments/tweet-feed :: tweet-list";
    }

    @DeleteMapping("/tweets/{tweetId}")
    @ResponseBody
    public ResponseEntity<Void> deleteTweet(@PathVariable("tweetId") String tweetId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        tweetService.deleteTweet(tweetId, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok().build();
    }
}