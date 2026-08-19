package com.joshi.twitterclone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.NotificationService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TweetService tweetService;
    private final NotificationService notificationService;

    @GetMapping("/u/{username}")
    public String viewProfile(@PathVariable("username") String username,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        ProfileDto profile = userService.getProfile(username, userDetails.getUsername());

        model.addAttribute("profile", profile);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(userDetails.getUsername(), 4));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));

        return "profile";
    }

    @PostMapping("/u/{username}/follow")
    public String followUser(@PathVariable("username") String username,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        userService.toggleFollow(userDetails.getUsername(), username);
        ProfileDto profile = userService.getProfile(username, userDetails.getUsername());
        model.addAttribute("profile", profile);

        return "fragments/follow-components :: follow-section";
    }

    @GetMapping("/u/{username}/edit")
    public String getEditProfileModal(@PathVariable("username") String username,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        if (!username.equalsIgnoreCase(userDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit another user's profile");
        }

        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        model.addAttribute("user", user);
        return "fragments/edit-profile-modal :: edit-modal";
    }

    @PostMapping("/u/{username}/edit")
    public String updateProfile(@PathVariable("username") String username,
                                @ModelAttribute EditProfileRequest request,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        if (!username.equalsIgnoreCase(userDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit another user's profile");
        }

        userService.updateProfile(username, request);
        ProfileDto profile = userService.getProfile(username, userDetails.getUsername());

        model.addAttribute("profile", profile);
        model.addAttribute("currentUserId", profile.getUser().getId());
        model.addAttribute("currentUsername", username);

        return "redirect:/u/" + username;
    }
}