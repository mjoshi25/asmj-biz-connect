package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PrivacyController {

    private final UserService userService;

    @GetMapping("/settings/privacy")
    public String viewPrivacySettings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);
        return "settings-privacy";
    }

    @PostMapping("/settings/privacy/update")
    public String updatePrivacySettings(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(value = "isPrivateAccount", defaultValue = "false") boolean isPrivateAccount,
                                        @RequestParam(value = "showEmailToPublic", defaultValue = "false") boolean showEmailToPublic,
                                        @RequestParam(value = "allowDirectMessagesFromEveryone", defaultValue = "true") boolean allowDms) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        userService.updateUserPrivacyPreferences(userDetails.getUsername(), isPrivateAccount, showEmailToPublic, allowDms);
        return "redirect:/settings/privacy?success=true";
    }
}