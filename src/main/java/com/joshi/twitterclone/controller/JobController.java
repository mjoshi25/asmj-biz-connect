package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.jobs.ApplicationStatus;
import com.joshi.twitterclone.model.jobs.JobApplication;
import com.joshi.twitterclone.model.jobs.JobListing;
import com.joshi.twitterclone.service.JobService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    @GetMapping
    public String viewJobs(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(value = "city", required = false) String city,
                           Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<JobListing> availableJobs = jobService.getAvailableJobs(city);
        List<JobApplication> myApplications = jobService.getApplicationsByApplicant(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("jobs", availableJobs);
        model.addAttribute("myApplications", myApplications);
        model.addAttribute("selectedCity", city != null ? city : "");

        return "jobs";
    }

    @PostMapping("/post")
    public String postJob(@AuthenticationPrincipal UserDetails userDetails,
                          @ModelAttribute JobListing jobListing,
                          @RequestParam(value = "skillsList", required = false) String skillsList,
                          @RequestParam(value = "logo", required = false) MultipartFile logo) {
        jobService.createJobListing(userDetails.getUsername(), jobListing, skillsList, logo);
        return "redirect:/jobs?posted=true";
    }

    @PostMapping("/apply")
    public String applyForJob(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("jobId") String jobId,
                              @RequestParam("fullName") String fullName,
                              @RequestParam("email") String email,
                              @RequestParam("phone") String phone,
                              @RequestParam("yearsOfExperience") int yearsOfExperience,
                              @RequestParam(value = "coverLetter", required = false) String coverLetter,
                              @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile) {
        jobService.submitApplication(userDetails.getUsername(), jobId, fullName, email, phone, yearsOfExperience, coverLetter, resumeFile);
        return "redirect:/jobs?applied=true";
    }

    @GetMapping("/manage")
    public String manageJobsAndApplicants(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<JobListing> myPostedJobs = jobService.getJobsPostedBy(username);
        List<JobApplication> receivedApplications = jobService.getApplicationsForRecruiter(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("postedJobs", myPostedJobs);
        model.addAttribute("applications", receivedApplications);

        return "job-manage";
    }

    @PostMapping("/applications/{applicationId}/status")
    public String updateStatus(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable("applicationId") String applicationId,
                               @RequestParam("status") ApplicationStatus status) {
        jobService.updateApplicationStatus(applicationId, userDetails.getUsername(), status);
        return "redirect:/jobs/manage";
    }
}