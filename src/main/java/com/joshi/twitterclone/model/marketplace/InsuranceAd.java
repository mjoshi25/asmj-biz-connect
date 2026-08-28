package com.joshi.twitterclone.model.marketplace;

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
@Document(collection = "insurance_ads")
public class InsuranceAd {

    @Id
    private String id;

    @Indexed
    private String insurerUsername;
    private String insurerDisplayName;
    private String providerCompany;

    private String title;
    private InsuranceType insuranceType;

    private BigDecimal baseAnnualPremium;
    private BigDecimal coverageAmount;

    private String policyHighlights;

    @Builder.Default
    private List<String> keyBenefits = new ArrayList<>();

    private String bannerImageUrl;
    private String brochurePdfUrl;

    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING_APPROVAL;
    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}