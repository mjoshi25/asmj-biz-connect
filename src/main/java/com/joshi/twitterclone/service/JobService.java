package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.jobs.ApplicationStatus;
import com.joshi.twitterclone.model.jobs.JobApplication;
import com.joshi.twitterclone.model.jobs.JobListing;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.repository.jobs.JobApplicationRepository;
import com.joshi.twitterclone.repository.jobs.JobListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobListingRepository jobListingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;

    public JobListing createJob(String username, JobListing job) {
        User user = userService.getUserByUsername(username);

        job.setPosterUsername(user.getUsername());
        job.setPosterDisplayName(user.getDisplayName());
        job.setStatus(ListingStatus.PENDING_APPROVAL);
        job.setCreatedAt(LocalDateTime.now());
        job.setApplicantCount(0);

        return jobListingRepository.save(job);
    }

    public JobListing updateJob(String username, String jobId, JobListing updated) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!job.getPosterUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this job");
        }

        job.setJobTitle(updated.getJobTitle());
        job.setCompanyName(updated.getCompanyName());
        job.setEmploymentType(updated.getEmploymentType());
        job.setWorkplaceType(updated.getWorkplaceType());
        job.setLocationCity(updated.getLocationCity());
        job.setMinSalary(updated.getMinSalary());
        job.setMaxSalary(updated.getMaxSalary());
        job.setJobDescription(updated.getJobDescription());
        job.setRequirements(updated.getRequirements());
        job.setRequiredSkills(updated.getRequiredSkills());
        job.setStatus(ListingStatus.PENDING_APPROVAL); // Re-trigger moderation on update

        return jobListingRepository.save(job);
    }

    public void deleteJob(String username, String jobId) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!job.getPosterUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this job");
        }

        jobListingRepository.delete(job);
    }

    public List<JobListing> getApprovedJobs(String city, String keyword) {
        List<JobListing> all = jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        return all.stream()
                .filter(j -> city == null || city.isBlank() || j.getLocationCity().equalsIgnoreCase(city.trim()))
                .filter(j -> keyword == null || keyword.isBlank() 
                        || j.getJobTitle().toLowerCase().contains(keyword.toLowerCase().trim())
                        || j.getCompanyName().toLowerCase().contains(keyword.toLowerCase().trim()))
                .toList();
    }

    public List<JobListing> getMyPostedJobs(String username) {
        return jobListingRepository.findByPosterUsernameOrderByCreatedAtDesc(username);
    }

    public List<JobListing> getPendingJobs() {
        return jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateJob(String jobId, boolean approve, String rejectionReason) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        job.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            job.setRejectionReason(rejectionReason);
        }
        jobListingRepository.save(job);
    }

    public JobApplication applyForJob(String applicantUsername, String jobId, JobApplication app) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        User user = userService.getUserByUsername(applicantUsername);

        app.setJobId(job.getId());
        app.setJobTitle(job.getJobTitle());
        app.setCompanyName(job.getCompanyName());
        app.setPosterUsername(job.getPosterUsername());
        app.setApplicantUsername(user.getUsername());
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDateTime.now());

        job.setApplicantCount(job.getApplicantCount() + 1);
        jobListingRepository.save(job);

        return jobApplicationRepository.save(app);
    }
}