package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
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

@Controller
@RequestMapping("/u")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final TweetService tweetService;
    private final EventService eventService;

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
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute EditProfileRequest request) {
        userService.updateProfile(userDetails.getUsername(), request);
        return "redirect:/u/" + userDetails.getUsername();
    }
}