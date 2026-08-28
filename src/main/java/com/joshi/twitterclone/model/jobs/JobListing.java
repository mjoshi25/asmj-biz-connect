package com.joshi.twitterclone.model.jobs;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_listings")
public class JobListing {

    @Id
    private String id;

    @Indexed
    private String posterUsername;
    private String posterDisplayName;
    private String companyName;
    private String companyLogoUrl;

    private String jobTitle;
    private EmploymentType employmentType;
    private WorkplaceType workplaceType;
    private String locationCity;

    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    @Builder.Default
    private String salaryCurrency = "INR";

    private String jobDescription;
    private String requirements;

    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING_APPROVAL;
    private String rejectionReason;

    @Builder.Default
    private int applicantCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}