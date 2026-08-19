package com.joshi.twitterclone.controller;

import java.io.IOException;

import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TimelineController {

    private final TweetService tweetService;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private static final int PAGE_SIZE = 10;

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Slice<Tweet> tweetSlice = tweetService.getTimelineSlice(userDetails.getUsername(), 0, PAGE_SIZE);

        model.addAttribute("tweets", tweetSlice);
        model.addAttribute("currentPage", 0);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(userDetails.getUsername(), 4));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));

        return "timeline";
    }

    @GetMapping("/tweets/feed")
    public String loadMoreTweets(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Slice<Tweet> tweetSlice = tweetService.getTimelineSlice(userDetails.getUsername(), page, PAGE_SIZE);

        model.addAttribute("tweets", tweetSlice);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUserId", currentUser.getId());

        return "fragments/tweet-feed :: tweet-list";
    }

    @PostMapping("/tweets")
    public String postTweet(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "content", required = false) String content,
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            Model model) throws IOException {
        String trimmed = content != null ? content.trim() : "";
        boolean hasImage = image != null && !image.isEmpty();

        if (trimmed.isEmpty() && !hasImage) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tweet cannot be empty.");
        }
        if (trimmed.length() > 280) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tweet exceeds 280 characters.");
        }

        User user = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet newTweet = tweetService.createTweetWithAsyncImage(userDetails.getUsername(), trimmed, image);

        model.addAttribute("tweet", newTweet);
        model.addAttribute("currentUserId", user.getId());

        return "fragments/tweet-components :: tweet-card";
    }

    @PostMapping("/tweets/{tweetId}/like")
    public String toggleLike(@PathVariable("tweetId") String tweetId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet updatedTweet = tweetService.toggleLike(tweetId, userDetails.getUsername());

        model.addAttribute("tweet", updatedTweet);
        model.addAttribute("currentUserId", currentUser.getId());

        return "fragments/tweet-components :: like-button";
    }

    @DeleteMapping("/tweets/{tweetId}")
    @ResponseBody
    public ResponseEntity<Void> deleteTweet(@PathVariable("tweetId") String tweetId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        tweetService.deleteTweet(tweetId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tweets/{tweetId}/lightbox")
    public String getTweetLightbox(@PathVariable("tweetId") String tweetId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        model.addAttribute("tweet", tweet);
        model.addAttribute("currentUserId", currentUser.getId());

        return "fragments/lightbox :: image-modal";
    }
}