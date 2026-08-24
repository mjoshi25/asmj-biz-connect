package com.joshi.twitterclone.config;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.DirectMessage;
import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.DirectMessageRepository;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceAdRepository;
import com.joshi.twitterclone.repository.marketplace.InsuranceQuoteRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleBookingRepository;
import com.joshi.twitterclone.repository.marketplace.VehicleListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository directMessageRepository;
    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleBookingRepository vehicleBookingRepository;
    private final InsuranceAdRepository insuranceAdRepository;
    private final InsuranceQuoteRepository insuranceQuoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking database state and executing granular seeders...");

        // 1. Seed Core Accounts if Missing
        createAccountIfMissing("admin", "ASMJ Administrator", "admin@asmjbizconnect.com", "Official platform administrator & marketplace review moderator.", Set.of("ROLE_USER", "ROLE_ADMIN"));
        createAccountIfMissing("alex_tech", "Alex Rivera", "alex@example.com", "Cloud Architect & Fleet Operator. Building scalable microservices.", Set.of("ROLE_USER"));
        createAccountIfMissing("sarah_designs", "Sarah Chen", "sarah@example.com", "Corporate Risk Advisor & Insurance Partner at Allied Shield.", Set.of("ROLE_USER"));

        // 2. Seed Posts if Empty
        if (tweetRepository.count() == 0) {
            log.info("Seeding initial feed posts...");
            User alex = userRepository.findByUsername("alex_tech").orElse(null);
            if (alex != null) {
                Tweet post1 = new Tweet();
                post1.setAuthorId(alex.getId());
                post1.setAuthorUsername(alex.getUsername());
                post1.setAuthorDisplayName(alex.getDisplayName());
                post1.setContent("Excited to launch our commercial EV fleet on ASMJ Biz Connect Marketplace! Check our listings for corporate inter-city bookings. #ASMJBizConnect #Innovation");
                post1.setLikesCount(14);
                post1.setRepliesCount(2);
                post1.setMediaStatus(MediaStatus.NONE);
                post1.setCreatedAt(LocalDateTime.now().minusDays(1));
                tweetRepository.save(post1);
            }
        }

        // 3. Seed Vehicle Listings if Empty
        if (vehicleListingRepository.count() == 0) {
            log.info("Seeding vehicle rental marketplace listings...");

            VehicleListing v1 = VehicleListing.builder()
                    .ownerUsername("alex_tech")
                    .ownerDisplayName("Alex Rivera")
                    .contactNumber("+91 9876543210")
                    .make("Hyundai")
                    .modelName("Creta SX (O)")
                    .year(2023)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Petrol")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Bengaluru")
                    .pickupAddress("Indiranagar 100ft Road, Bengaluru")
                    .dailyRentalRate(BigDecimal.valueOf(2800.00))
                    .securityDeposit(BigDecimal.valueOf(5000.00))
                    .description("Pristine condition SUV with panoramic sunroof, ventilated seats, and full ADAS safety package.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            VehicleListing v2 = VehicleListing.builder()
                    .ownerUsername("alex_tech")
                    .ownerDisplayName("Alex Rivera")
                    .contactNumber("+91 9876543210")
                    .make("Tata")
                    .modelName("Nexon EV Max")
                    .year(2024)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Electric")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Mumbai")
                    .pickupAddress("BKC Commercial Hub, Mumbai")
                    .dailyRentalRate(BigDecimal.valueOf(3200.00))
                    .securityDeposit(BigDecimal.valueOf(6000.00))
                    .description("Zero-emission executive EV with fast-charging support and 400km real-world range.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            VehicleListing v3 = VehicleListing.builder()
                    .ownerUsername("sarah_designs")
                    .ownerDisplayName("Sarah Chen")
                    .contactNumber("+91 9123456780")
                    .make("Mahindra")
                    .modelName("Thar 4x4")
                    .year(2023)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Diesel")
                    .transmission("Manual")
                    .seatingCapacity(4)
                    .locationCity("Bengaluru")
                    .pickupAddress("Koramangala 4th Block, Bengaluru")
                    .dailyRentalRate(BigDecimal.valueOf(3500.00))
                    .securityDeposit(BigDecimal.valueOf(7000.00))
                    .description("Adventure-ready 4x4 SUV suited for rough terrain or corporate off-site expeditions.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.PENDING_APPROVAL)
                    .createdAt(LocalDateTime.now().minusHours(3))
                    .build();

            vehicleListingRepository.saveAll(List.of(v1, v2, v3));

            // Seed a sample booking
            VehicleBooking booking = VehicleBooking.builder()
                    .listingId(v1.getId())
                    .vehicleSummary("2023 Hyundai Creta SX (O)")
                    .renterUsername("admin")
                    .ownerUsername("alex_tech")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(4))
                    .totalDays(3)
                    .totalAmount(BigDecimal.valueOf(8400.00))
                    .customerPhone("+91 9988776655")
                    .drivingLicenseNumber("DL-0420190012345")
                    .bookingStatus("CONFIRMED")
                    .bookedAt(LocalDateTime.now().minusHours(1))
                    .build();
            vehicleBookingRepository.save(booking);
        }

        // 4. Seed Insurance Ads if Empty
        if (insuranceAdRepository.count() == 0) {
            log.info("Seeding insurance advertisement catalog...");

            InsuranceAd ad1 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Allied National General Insurance")
                    .title("Corporate Commercial Fleet Protection 360")
                    .insuranceType(InsuranceType.VEHICLE_COMPREHENSIVE)
                    .baseAnnualPremium(BigDecimal.valueOf(14500.00))
                    .coverageAmount(BigDecimal.valueOf(1500000.00))
                    .policyHighlights("Full bumper-to-bumper zero depreciation coverage with 24/7 pan-India roadside assistance and engine protection.")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(6))
                    .build();

            InsuranceAd ad2 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Tata AIG Risk Solutions")
                    .title("Business Executive Group Health Shield")
                    .insuranceType(InsuranceType.HEALTH_FAMILY)
                    .baseAnnualPremium(BigDecimal.valueOf(22000.00))
                    .coverageAmount(BigDecimal.valueOf(2500000.00))
                    .policyHighlights("Cashless hospitalization across 8000+ top hospitals with no room-rent capping and OPD consult benefits.")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .build();

            InsuranceAd ad3 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("HDFC ERGO Enterprise")
                    .title("B2B Cyber & Professional Indemnity Cover")
                    .insuranceType(InsuranceType.BUSINESS_LIABILITY)
                    .baseAnnualPremium(BigDecimal.valueOf(35000.00))
                    .coverageAmount(BigDecimal.valueOf(5000000.00))
                    .policyHighlights("Coverage against data breaches, liability claims, ransomware recovery, and legal defense costs.")
                    .status(ListingStatus.PENDING_APPROVAL)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .build();

            insuranceAdRepository.saveAll(List.of(ad1, ad2, ad3));
        }

        log.info("Database synchronization check complete.");
    }

    private void createAccountIfMissing(String username, String displayName, String email, String bio, Set<String> roles) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setBio(bio);
            user.setLocation("Bengaluru, India");
            user.setRoles(new HashSet<>(roles));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Seeded user account: @{}", username);
        }
    }
}