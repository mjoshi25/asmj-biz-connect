package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.service.AdminAnalyticsService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAnalyticsService adminAnalyticsService;
    private final UserService userService;

    @GetMapping("/analytics")
    public String viewAdminAnalytics(Model model) {
        Map<String, Object> stats = adminAnalyticsService.getAnalyticsSummary();
        model.addAttribute("stats", stats);
        model.addAttribute("usersList", stats.get("usersList"));
        model.addAttribute("pendingEvents", stats.get("pendingEvents"));
        model.addAttribute("approvedEvents", stats.get("approvedEvents"));
        model.addAttribute("rejectedEvents", stats.get("rejectedEvents"));
        return "admin-analytics";
    }

    @PostMapping("/users/{username}/toggle")
    public String toggleUserStatus(@PathVariable("username") String username) {
        userService.toggleUserStatus(username);
        return "redirect:/admin/analytics";
    }
}