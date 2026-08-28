package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAnalyticsService analyticsService;
    private final UserService userService;
    private final MarketplaceService marketplaceService;
    private final ProductServiceItemService productServiceItemService;
    private final JobService jobService;

    @GetMapping("/analytics")
    public String viewAdminAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("stats", analyticsService.getPlatformAnalytics());
        model.addAttribute("pendingVehicles", marketplaceService.getPendingVehicles());
        model.addAttribute("pendingAds", marketplaceService.getPendingInsuranceAds());
        model.addAttribute("pendingProducts", productServiceItemService.getPendingItems());
        model.addAttribute("pendingJobs", jobService.getPendingJobs());

        return "admin-analytics";
    }
}