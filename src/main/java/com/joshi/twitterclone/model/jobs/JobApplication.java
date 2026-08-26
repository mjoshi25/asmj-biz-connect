package com.joshi.twitterclone.model.jobs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_applications")
public class JobApplication {

    @Id
    private String id;

    @Indexed
    private String jobId;
    private String jobTitle;
    private String companyName;

    @Indexed
    private String applicantUsername;
    private String applicantFullName;
    private String applicantEmail;
    private String applicantPhone;

    private String resumeUrl;
    private String coverLetterNote;
    private int yearsOfExperience;

    @Indexed
    private String posterUsername;

    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (appliedAt == null) return "";
        return appliedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}