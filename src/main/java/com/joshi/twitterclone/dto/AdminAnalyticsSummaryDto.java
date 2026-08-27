package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsSummaryDto {
    private long totalUsers;
    private long totalTweets;
    private long totalConversations;

    // Marketplace Metrics
    private long totalVehicles;
    private long pendingVehicles;
    private long totalVehicleBookings;
    private BigDecimal totalVehicleRentalVolume;

    // Insurance Metrics
    private long totalInsuranceAds;
    private long pendingInsuranceAds;
    private long totalInsuranceQuotes;

    // Products & Services Metrics
    private long totalProductsServices;
    private long pendingProductsServices;

    // Recruitment & Events Metrics
    private long totalJobs;
    private long totalJobApplications;
    private long totalEvents;
    private long totalEventBookings;
    private BigDecimal totalEventTicketRevenue;
}