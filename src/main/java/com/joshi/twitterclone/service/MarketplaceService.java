package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.repository.marketplace.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final VehicleListingRepository vehicleRepository;
    private final VehicleBookingRepository bookingRepository;
    private final InsuranceAdRepository insuranceAdRepository;
    private final InsuranceQuoteRepository quoteRepository;
    private final UserService userService;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    // --- VEHICLE RENTALS ---

    public VehicleListing createVehicleListing(String username, VehicleListing listing, List<MultipartFile> images) {
        User user = userService.getUserByUsername(username);
        listing.setOwnerUsername(user.getUsername());
        listing.setOwnerDisplayName(user.getDisplayName());
        listing.setStatus(ListingStatus.PENDING_APPROVAL);
        listing.setCreatedAt(LocalDateTime.now());

        List<String> uploadedUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    uploadedUrls.add(fileStorageService.saveImageOptimized(img));
                }
            }
        }
        listing.setImageUrls(uploadedUrls);
        return vehicleRepository.save(listing);
    }

    public List<VehicleListing> getApprovedVehicles(String city) {
        if (city != null && !city.isBlank()) {
            return vehicleRepository.findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus.APPROVED, city);
        }
        return vehicleRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public VehicleBooking bookVehicle(String renterUsername, String listingId, LocalDate startDate, LocalDate endDate, String phone, String license) {
        VehicleListing vehicle = vehicleRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle listing not found"));

        if (vehicle.getStatus() != ListingStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing is not available for booking");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;

        BigDecimal totalAmount = vehicle.getDailyRentalRate().multiply(BigDecimal.valueOf(days));

        VehicleBooking booking = VehicleBooking.builder()
                .listingId(listingId)
                .vehicleSummary(vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModelName())
                .renterUsername(renterUsername)
                .ownerUsername(vehicle.getOwnerUsername())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(days)
                .totalAmount(totalAmount)
                .customerPhone(phone)
                .drivingLicenseNumber(license)
                .bookingStatus("CONFIRMED")
                .bookedAt(LocalDateTime.now())
                .build();

        // Automatically start chat between renter and vehicle owner
        Conversation convo = directMessageService.getOrCreateDirectConversation(renterUsername, vehicle.getOwnerUsername());
        directMessageService.sendMessage(renterUsername, convo.getId(), 
                "Hello! I have booked your " + booking.getVehicleSummary() + " from " + startDate + " to " + endDate + ". Looking forward to pickup!", null);

        return bookingRepository.save(booking);
    }

    // --- INSURANCE ADS & QUOTES ---

    public InsuranceAd createInsuranceAd(String username, InsuranceAd ad, MultipartFile banner, MultipartFile brochure) {
        User user = userService.getUserByUsername(username);
        ad.setInsurerUsername(user.getUsername());
        ad.setInsurerDisplayName(user.getDisplayName());
        ad.setStatus(ListingStatus.PENDING_APPROVAL);
        ad.setCreatedAt(LocalDateTime.now());

        if (banner != null && !banner.isEmpty()) {
            ad.setBannerImageUrl(fileStorageService.saveImageOptimized(banner));
        }
        if (brochure != null && !brochure.isEmpty()) {
            ad.setBrochureUrl(fileStorageService.saveFile(brochure));
        }

        return insuranceAdRepository.save(ad);
    }

    public List<InsuranceAd> getApprovedInsuranceAds() {
        return insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public InsuranceQuote calculateAndRequestQuote(String username, String adId, String name, String email, String phone, int age, BigDecimal estimatedValue) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance Ad not found"));

        // Premium Calculation Algorithm based on policy type and value
        BigDecimal multiplier = BigDecimal.valueOf(0.035); // 3.5% baseline
        if (ad.getInsuranceType() == InsuranceType.HEALTH_INDIVIDUAL || ad.getInsuranceType() == InsuranceType.HEALTH_FAMILY) {
            multiplier = age > 45 ? BigDecimal.valueOf(0.055) : BigDecimal.valueOf(0.028);
        }

        BigDecimal calculatedPremium = (estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) > 0)
                ? estimatedValue.multiply(multiplier).setScale(2, RoundingMode.HALF_UP)
                : ad.getBaseAnnualPremium();

        // Start direct conversation with Insurer
        Conversation convo = directMessageService.getOrCreateDirectConversation(username, ad.getInsurerUsername());
        String initialMsg = String.format("Hi! I generated an online quote for '%s'. Estimated Sum Insured: ₹%s. Estimated Premium: ₹%s/yr. Let's discuss policy terms.",
                ad.getTitle(), estimatedValue != null ? estimatedValue : "Standard", calculatedPremium);
        directMessageService.sendMessage(username, convo.getId(), initialMsg, null);

        InsuranceQuote quote = InsuranceQuote.builder()
                .adId(adId)
                .adTitle(ad.getTitle())
                .insurerUsername(ad.getInsurerUsername())
                .applicantUsername(username)
                .applicantName(name)
                .applicantEmail(email)
                .applicantPhone(phone)
                .applicantAge(age)
                .estimatedValueOrSumInsured(estimatedValue)
                .calculatedQuotePremium(calculatedPremium)
                .status("GENERATED")
                .associatedConversationId(convo.getId())
                .requestedAt(LocalDateTime.now())
                .build();

        return quoteRepository.save(quote);
    }

    // --- ADMIN APPROVAL WORKFLOWS ---

    public List<VehicleListing> getPendingVehicles() {
        return vehicleRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public List<InsuranceAd> getPendingInsuranceAds() {
        return insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateVehicle(String vehicleId, boolean approve, String rejectionReason) {
        VehicleListing vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        vehicle.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) vehicle.setRejectionReason(rejectionReason);
        vehicleRepository.save(vehicle);
    }

    public void moderateInsuranceAd(String adId, boolean approve, String rejectionReason) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance Ad not found"));
        ad.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) ad.setRejectionReason(rejectionReason);
        insuranceAdRepository.save(ad);
    }
}