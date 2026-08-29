package com.joshi.twitterclone.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsDTO {
    private long totalUsers;
    private long totalListings;
    private long totalOrders;
    private long totalConversations;
}