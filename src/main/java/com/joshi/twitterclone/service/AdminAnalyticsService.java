package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.AdminAnalyticsSummaryDto;
import com.joshi.twitterclone.model.events.EventBooking;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.marketplace.VehicleBooking;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.repository.events.EventBookingRepository;
import com.joshi.twitterclone.repository.events.EventListingRepository;
import com.joshi.twitterclone.repository.jobs.JobApplicationRepository;
import com.joshi.twitterclone.repository.jobs.JobListingRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceAdRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceQuoteRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleBookingRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleListingRepository;
import com.joshi.twitterclone.repository.products.ProductServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final ConversationRepository conversationRepository;
    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleBookingRepository vehicleBookingRepository;
    private final InsuranceAdRepository insuranceAdRepository;
    private final InsuranceQuoteRepository insuranceQuoteRepository;
    private final ProductServiceRepository productServiceRepository;
    private final JobListingRepository jobListingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final EventListingRepository eventListingRepository;
    private final EventBookingRepository eventBookingRepository;

    public AdminAnalyticsSummaryDto getPlatformAnalytics() {
        List<VehicleBooking> vehicleBookings = vehicleBookingRepository.findAll();
        BigDecimal vehicleVolume = vehicleBookings.stream()
                .map(VehicleBooking::getTotalAmount)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EventBooking> eventBookings = eventBookingRepository.findAll();
        BigDecimal eventRevenue = eventBookings.stream()
                .map(EventBooking::getTotalAmount)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminAnalyticsSummaryDto.builder()
                .totalUsers(userRepository.count())
                .totalTweets(tweetRepository.count())
                .totalConversations(conversationRepository.count())
                .totalVehicles(vehicleListingRepository.count())
                .pendingVehicles(vehicleListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL).size())
                .totalVehicleBookings(vehicleBookings.size())
                .totalVehicleRentalVolume(vehicleVolume)
                .totalInsuranceAds(insuranceAdRepository.count())
                .pendingInsuranceAds(insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL).size())
                .totalInsuranceQuotes(insuranceQuoteRepository.count())
                .totalProductsServices(productServiceRepository.count())
                .pendingProductsServices(productServiceRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL).size())
                .totalJobs(jobListingRepository.count())
                .totalJobApplications(jobApplicationRepository.count())
                .totalEvents(eventListingRepository.count())
                .totalEventBookings(eventBookings.size())
                .totalEventTicketRevenue(eventRevenue)
                .build();
    }
}