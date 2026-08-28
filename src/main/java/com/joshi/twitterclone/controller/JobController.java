package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.jobs.JobApplication;
import com.joshi.twitterclone.model.jobs.JobListing;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.JobService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserService userService;
    private final EventService eventService;

    @GetMapping
    public String viewJobs(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(value = "city", required = false) String city,
                           @RequestParam(value = "q", required = false) String query,
                           Model model) {
        String username = userDetails != null ? userDetails.getUsername() : "";
        User currentUser = userService.getUserByUsername(username);

        List<JobListing> jobs = jobService.getApprovedJobs(city, query);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("jobs", jobs != null ? jobs : Collections.emptyList());
        model.addAttribute("selectedCity", city != null ? city : "");
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "jobs";
    }

    @PostMapping("/post")
    public String postJob(@AuthenticationPrincipal UserDetails userDetails,
                          @ModelAttribute JobListing jobListing) {
        jobService.createJob(userDetails.getUsername(), jobListing);
        return "redirect:/jobs/manage?posted=true";
    }

    @PostMapping("/update/{id}")
    public String updateJob(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable("id") String jobId,
                            @ModelAttribute JobListing jobListing) {
        jobService.updateJob(userDetails.getUsername(), jobId, jobListing);
        return "redirect:/jobs/manage?updated=true";
    }

    @PostMapping("/delete/{id}")
    public String deleteJob(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable("id") String jobId) {
        jobService.deleteJob(userDetails.getUsername(), jobId);
        return "redirect:/jobs/manage?deleted=true";
    }

    @GetMapping("/manage")
    public String viewRecruiterDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<JobListing> myListings = jobService.getMyPostedJobs(userDetails.getUsername());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myListings", myListings);

        return "jobs-manage";
    }

    @PostMapping("/apply")
    public String applyJob(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam("jobId") String jobId,
                           @ModelAttribute JobApplication application) {
        jobService.applyForJob(userDetails.getUsername(), jobId, application);
        return "redirect:/jobs?applied=true";
    }

    @PostMapping("/admin/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateJob(@PathVariable("id") String id,
                              @RequestParam("approve") boolean approve,
                              @RequestParam(value = "rejectionReason", required = false) String reason) {
        jobService.moderateJob(id, approve, reason);
        return "redirect:/admin/analytics";
    }
}