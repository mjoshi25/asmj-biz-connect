package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
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
@RequestMapping("/u")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final TweetService tweetService;

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable("username") String username,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        ProfileDto profile = userService.getProfile(username, currentUsername);
        User currentUser = currentUsername != null ? userService.getUserByUsername(currentUsername) : null;

        model.addAttribute("profile", profile);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(currentUsername, 4));

        return "profile";
    }

    @PostMapping("/{username}/follow")
    public String toggleFollow(@PathVariable("username") String targetUsername,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                               @RequestHeader(value = "Referer", required = false) String referer,
                               Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;
        boolean isFollowing = userService.toggleFollow(targetUsername, currentUsername);
        ProfileDto profile = userService.getProfile(targetUsername, currentUsername);

        if ("true".equalsIgnoreCase(hxRequest)) {
            model.addAttribute("profile", profile);
            model.addAttribute("targetUsername", targetUsername);
            model.addAttribute("isFollowing", isFollowing);
            return "fragments/profile-actions :: follow-button";
        }

        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/u/" + targetUsername;
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute EditProfileRequest request) {
        if (userDetails != null) {
            userService.updateProfile(userDetails.getUsername(), request);
            return "redirect:/u/" + userDetails.getUsername();
        }
        return "redirect:/login";
    }
}