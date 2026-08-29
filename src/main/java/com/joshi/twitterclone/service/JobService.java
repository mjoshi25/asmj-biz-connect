package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.jobs.JobApplication;
import com.joshi.twitterclone.model.jobs.JobListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.repository.jobs.JobApplicationRepository;
import com.joshi.twitterclone.repository.jobs.JobListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobListingRepository jobListingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // Matches JobController line 51: jobService.createJob(...)
    public JobListing createJob(String username, JobListing job) {
        return postJob(username, job);
    }

    public JobListing postJob(String username, JobListing job) {
        User user = userService.getUserByUsername(username);
        job.setPosterUsername(user.getUsername());
        job.setStatus(ListingStatus.PENDING_APPROVAL);
        job.setCreatedAt(LocalDateTime.now());
        job.setApplicantCount(0);
        return jobListingRepository.save(job);
    }

    // Matches JobController line 37: jobService.getApprovedJobs(search, location)
    public List<JobListing> getApprovedJobs(String search, String location) {
        List<JobListing> all = jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        return all.stream()
                .filter(j -> {
                    boolean matchesSearch = (search == null || search.isBlank() ||
                            j.getJobTitle().toLowerCase().contains(search.toLowerCase()) ||
                            j.getCompanyName().toLowerCase().contains(search.toLowerCase()));
                    boolean matchesLoc = (location == null || location.isBlank() ||
                            j.getLocationCity().toLowerCase().contains(location.toLowerCase()));
                    return matchesSearch && matchesLoc;
                })
                .toList();
    }

    public List<JobListing> getApprovedJobs() {
        return jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public List<JobListing> getPendingJobs() {
        return jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    // Matches JobController line 73: jobService.getMyPostedJobs(username)
    public List<JobListing> getMyPostedJobs(String username) {
        return getJobsByUser(username);
    }

    public List<JobListing> getJobsByUser(String username) {
        return jobListingRepository.findByPosterUsernameOrderByCreatedAtDesc(username.toLowerCase().trim());
    }

    // Matches JobController line 85: jobService.applyForJob(username, jobId, application)
    public JobApplication applyForJob(String username, String jobId, JobApplication application) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job listing not found"));

        User applicant = userService.getUserByUsername(username);

        application.setJobId(job.getId());
        application.setJobTitle(job.getJobTitle());
        application.setCompanyName(job.getCompanyName());
        application.setApplicantUsername(applicant.getUsername());
        application.setApplicantFullName(applicant.getDisplayName());
        application.setAppliedAt(LocalDateTime.now());

        JobApplication saved = jobApplicationRepository.save(application);

        job.setApplicantCount(job.getApplicantCount() + 1);
        jobListingRepository.save(job);

        notificationService.sendNotification(job.getPosterUsername(), "New Job Application", 
                applicant.getDisplayName() + " applied for " + job.getJobTitle(), "/jobs/manage");

        return saved;
    }

    public void moderateJob(String jobId, boolean approve, String rejectionReason) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        job.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            job.setRejectionReason(rejectionReason);
        }
        jobListingRepository.save(job);

        String msg = approve ? "Your job opening '" + job.getJobTitle() + "' has been approved!" : "Your job opening was rejected: " + rejectionReason;
        notificationService.sendNotification(job.getPosterUsername(), "Job Moderation Update", msg, "/jobs/manage");
    }

    public JobListing updateJob(String jobId, String username, JobListing updatedData) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!job.getPosterUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        job.setJobTitle(updatedData.getJobTitle());
        job.setCompanyName(updatedData.getCompanyName());
        job.setLocationCity(updatedData.getLocationCity());
        job.setEmploymentType(updatedData.getEmploymentType());
        job.setWorkplaceType(updatedData.getWorkplaceType());
        job.setMinSalary(updatedData.getMinSalary());
        job.setMaxSalary(updatedData.getMaxSalary());
        job.setJobDescription(updatedData.getJobDescription());
        job.setRequirements(updatedData.getRequirements());
        job.setStatus(ListingStatus.PENDING_APPROVAL);

        return jobListingRepository.save(job);
    }

    public void deleteJob(String jobId, String username) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!job.getPosterUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
        jobListingRepository.delete(job);
    }
}