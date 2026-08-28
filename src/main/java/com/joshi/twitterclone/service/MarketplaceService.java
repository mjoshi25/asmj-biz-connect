package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceAdRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceQuoteRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleBookingRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleBookingRepository vehicleBookingRepository;
    private final InsuranceAdRepository insuranceAdRepository;
    private final InsuranceQuoteRepository insuranceQuoteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DirectMessageService directMessageService;

    // ==========================================
    // VEHICLE RENTALS
    // ==========================================

    public VehicleListing createVehicleListing(String username, VehicleListing listing, List<MultipartFile> images) {
        User owner = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        listing.setOwnerUsername(owner.getUsername());
        listing.setOwnerDisplayName(owner.getDisplayName());
        listing.setStatus(ListingStatus.PENDING_APPROVAL);
        listing.setCreatedAt(LocalDateTime.now());

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    imageUrls.add(fileStorageService.saveImageOptimized(file));
                }
            }
            listing.setImageUrls(imageUrls);
        }

        return vehicleListingRepository.save(listing);
    }

    public VehicleListing updateVehicle(String username, String vehicleId, VehicleListing updated) {
        VehicleListing listing = vehicleListingRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle listing not found"));

        if (!listing.getOwnerUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this vehicle listing");
        }

        listing.setMake(updated.getMake());
        listing.setModelName(updated.getModelName());
        listing.setYear(updated.getYear());
        listing.setVehicleType(updated.getVehicleType());
        listing.setFuelType(updated.getFuelType());
        listing.setTransmission(updated.getTransmission());
        listing.setSeatingCapacity(updated.getSeatingCapacity());
        listing.setDailyRentalRate(updated.getDailyRentalRate());
        listing.setSecurityDeposit(updated.getSecurityDeposit());
        listing.setLocationCity(updated.getLocationCity());
        listing.setPickupAddress(updated.getPickupAddress());
        listing.setDescription(updated.getDescription());
        listing.setStatus(ListingStatus.PENDING_APPROVAL); // Re-trigger moderation on update

        return vehicleListingRepository.save(listing);
    }

    public void deleteVehicle(String username, String vehicleId) {
        VehicleListing listing = vehicleListingRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle listing not found"));

        if (!listing.getOwnerUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this vehicle listing");
        }

        vehicleListingRepository.delete(listing);
    }

    public List<VehicleListing> getApprovedVehicles(String city) {
        List<VehicleListing> all = vehicleListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        if (city == null || city.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(v -> v.getLocationCity() != null && v.getLocationCity().equalsIgnoreCase(city.trim()))
                .toList();
    }

    public List<VehicleListing> getPendingVehicles() {
        return vehicleListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateVehicle(String vehicleId, boolean approve, String rejectionReason) {
        VehicleListing listing = vehicleListingRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle listing not found"));

        listing.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            listing.setRejectionReason(rejectionReason);
        }
        vehicleListingRepository.save(listing);
    }

    public VehicleBooking bookVehicle(String renterUsername, String listingId, LocalDate startDate, LocalDate endDate, String phone, String license) {
        VehicleListing vehicle = vehicleListingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle listing not found"));

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be prior to start date");
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate));
        BigDecimal total = vehicle.getDailyRentalRate().multiply(BigDecimal.valueOf(days));

        VehicleBooking booking = VehicleBooking.builder()
                .listingId(vehicle.getId())
                .vehicleSummary(String.format("%d %s %s", vehicle.getYear(), vehicle.getMake(), vehicle.getModelName()))
                .renterUsername(renterUsername)
                .ownerUsername(vehicle.getOwnerUsername())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays((int) days)
                .totalAmount(total)
                .customerPhone(phone)
                .drivingLicenseNumber(license)
                .bookingStatus("CONFIRMED")
                .bookedAt(LocalDateTime.now())
                .build();

        VehicleBooking saved = vehicleBookingRepository.save(booking);

        var convo = directMessageService.getOrCreateDirectConversation(renterUsername, vehicle.getOwnerUsername());
        String msg = String.format("🚗 New Vehicle Booking: %s (%s to %s, %d days, Total: ₹%s). Contact: %s. License: %s.",
                booking.getVehicleSummary(), startDate, endDate, days, total, phone, license);
        directMessageService.sendMessage(renterUsername, convo.getId(), msg, null);

        return saved;
    }

    // ==========================================
    // CORPORATE INSURANCE
    // ==========================================

    public InsuranceAd createInsuranceAd(String username, InsuranceAd ad, MultipartFile banner, MultipartFile brochure) {
        User insurer = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ad.setInsurerUsername(insurer.getUsername());
        ad.setInsurerDisplayName(insurer.getDisplayName());
        ad.setStatus(ListingStatus.PENDING_APPROVAL);
        ad.setCreatedAt(LocalDateTime.now());

        if (banner != null && !banner.isEmpty()) {
            ad.setBannerImageUrl(fileStorageService.saveImageOptimized(banner));
        }
        if (brochure != null && !brochure.isEmpty()) {
            ad.setBrochurePdfUrl(fileStorageService.saveFile(brochure));
        }

        return insuranceAdRepository.save(ad);
    }

    public InsuranceAd updateInsuranceAd(String username, String adId, InsuranceAd updated) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance ad not found"));

        if (!ad.getInsurerUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this insurance ad");
        }

        ad.setTitle(updated.getTitle());
        ad.setProviderCompany(updated.getProviderCompany());
        ad.setInsuranceType(updated.getInsuranceType());
        ad.setBaseAnnualPremium(updated.getBaseAnnualPremium());
        ad.setCoverageAmount(updated.getCoverageAmount());
        ad.setPolicyHighlights(updated.getPolicyHighlights());
        ad.setStatus(ListingStatus.PENDING_APPROVAL); // Re-trigger moderation on update

        return insuranceAdRepository.save(ad);
    }

    public void deleteInsuranceAd(String username, String adId) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance ad not found"));

        if (!ad.getInsurerUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this insurance ad");
        }

        insuranceAdRepository.delete(ad);
    }

    public List<InsuranceAd> getApprovedInsuranceAds() {
        return insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public List<InsuranceAd> getPendingInsuranceAds() {
        return insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateInsuranceAd(String adId, boolean approve, String rejectionReason) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance ad not found"));

        ad.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            ad.setRejectionReason(rejectionReason);
        }
        insuranceAdRepository.save(ad);
    }

    public InsuranceQuote calculateAndRequestQuote(String applicantUsername, String adId, String name, String email, String phone, int age, BigDecimal estimatedValue) {
        InsuranceAd ad = insuranceAdRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insurance ad not found"));

        BigDecimal calculatedPremium = ad.getBaseAnnualPremium();
        if (estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) > 0) {
            calculatedPremium = calculatedPremium.add(estimatedValue.multiply(BigDecimal.valueOf(0.025)));
        }

        InsuranceQuote quote = InsuranceQuote.builder()
                .adId(ad.getId())
                .adTitle(ad.getTitle())
                .insurerUsername(ad.getInsurerUsername())
                .applicantUsername(applicantUsername)
                .applicantName(name)
                .applicantEmail(email)
                .applicantPhone(phone)
                .applicantAge(age)
                .estimatedValueOrSumInsured(estimatedValue != null ? estimatedValue : BigDecimal.ZERO)
                .calculatedQuotePremium(calculatedPremium)
                .status("GENERATED")
                .requestedAt(LocalDateTime.now())
                .build();

        InsuranceQuote saved = insuranceQuoteRepository.save(quote);

        var convo = directMessageService.getOrCreateDirectConversation(applicantUsername, ad.getInsurerUsername());
        String msg = String.format("🛡️ Insurance Quote Request for '%s': Base Cover: ₹%s. Declared Base Value: ₹%s. Estimated Premium: ₹%s/year. Applicant: %s (%s, Phone: %s, Age: %d).",
                ad.getTitle(), ad.getCoverageAmount(), quote.getEstimatedValueOrSumInsured(), calculatedPremium, name, email, phone, age);
        directMessageService.sendMessage(applicantUsername, convo.getId(), msg, null);

        return saved;
    }
}