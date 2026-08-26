package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobListingRepository jobListingRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserService userService;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    public JobListing createJobListing(String username, JobListing listing, String skillsCommaSeparated, MultipartFile logo) {
        User user = userService.getUserByUsername(username);

        listing.setPosterUsername(user.getUsername());
        listing.setPosterDisplayName(user.getDisplayName());
        listing.setStatus(ListingStatus.APPROVED);
        listing.setApplicantCount(0);
        listing.setCreatedAt(LocalDateTime.now());

        if (skillsCommaSeparated != null && !skillsCommaSeparated.isBlank()) {
            List<String> skills = Arrays.stream(skillsCommaSeparated.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            listing.setRequiredSkills(skills);
        }

        if (logo != null && !logo.isEmpty()) {
            listing.setCompanyLogoUrl(fileStorageService.saveImageOptimized(logo));
        }

        return jobListingRepository.save(listing);
    }

    public List<JobListing> getAvailableJobs(String city) {
        if (city != null && !city.isBlank()) {
            return jobListingRepository.findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus.APPROVED, city);
        }
        return jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public List<JobListing> getJobsPostedBy(String username) {
        return jobListingRepository.findByPosterUsernameOrderByCreatedAtDesc(username);
    }

    public JobApplication submitApplication(String applicantUsername, String jobId, String fullName, String email, 
                                            String phone, int experienceYears, String coverLetter, MultipartFile resumeFile) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job listing not found"));

        if (applicationRepository.findByJobIdAndApplicantUsername(jobId, applicantUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied to this position.");
        }

        String resumeUrl = null;
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeUrl = fileStorageService.saveFile(resumeFile);
        }

        JobApplication application = JobApplication.builder()
                .jobId(jobId)
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .posterUsername(job.getPosterUsername())
                .applicantUsername(applicantUsername)
                .applicantFullName(fullName)
                .applicantEmail(email)
                .applicantPhone(phone)
                .yearsOfExperience(experienceYears)
                .coverLetterNote(coverLetter)
                .resumeUrl(resumeUrl)
                .status(ApplicationStatus.SUBMITTED)
                .appliedAt(LocalDateTime.now())
                .build();

        JobApplication saved = applicationRepository.save(application);

        job.setApplicantCount(job.getApplicantCount() + 1);
        jobListingRepository.save(job);

        // Open chat stream between applicant and recruiter
        Conversation convo = directMessageService.getOrCreateDirectConversation(applicantUsername, job.getPosterUsername());
        String notificationMessage = String.format("Hello! I submitted an application for the '%s' position at %s. Looking forward to discussing this opportunity!",
                job.getJobTitle(), job.getCompanyName());
        directMessageService.sendMessage(applicantUsername, convo.getId(), notificationMessage, null);

        return saved;
    }

    public List<JobApplication> getApplicationsForRecruiter(String recruiterUsername) {
        return applicationRepository.findByPosterUsernameOrderByAppliedAtDesc(recruiterUsername);
    }

    public List<JobApplication> getApplicationsByApplicant(String applicantUsername) {
        return applicationRepository.findByApplicantUsernameOrderByAppliedAtDesc(applicantUsername);
    }

    public void updateApplicationStatus(String applicationId, String recruiterUsername, ApplicationStatus newStatus) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getPosterUsername().equalsIgnoreCase(recruiterUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this application");
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);

        // Automated notification to candidate
        Conversation convo = directMessageService.getOrCreateDirectConversation(recruiterUsername, application.getApplicantUsername());
        String updateMsg = String.format("Update regarding your application for %s at %s: Status changed to [%s].",
                application.getJobTitle(), application.getCompanyName(), newStatus.name().replace("_", " "));
        directMessageService.sendMessage(recruiterUsername, convo.getId(), updateMsg, null);
    }
}