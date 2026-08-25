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
import com.joshi.twitterclone.repository.marketplace.*;
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
    private final MarketplaceProductRepository productRepository;
    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleBookingRepository vehicleBookingRepository;
    private final InsuranceAdRepository insuranceAdRepository;
    private final InsuranceQuoteRepository insuranceQuoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking database state and executing granular seeders...");

        // 1. Seed Core Accounts
        createAccountIfMissing("admin", "ASMJ Administrator", "admin@asmjbizconnect.com", "Official platform administrator & marketplace review moderator.", Set.of("ROLE_USER", "ROLE_ADMIN"));
        createAccountIfMissing("alex_tech", "Alex Rivera", "alex@example.com", "Cloud Architect & Fleet Operator. Building scalable microservices.", Set.of("ROLE_USER"));
        createAccountIfMissing("sarah_designs", "Sarah Chen", "sarah@example.com", "Corporate Risk Advisor & Insurance Partner at Allied Shield.", Set.of("ROLE_USER"));
        createAccountIfMissing("john_doe", "John Doe", "john@example.com", "Logistics Coordinator & Business Consultant.", Set.of("ROLE_USER"));

        // 2. Seed Posts
        if (tweetRepository.count() == 0) {
            log.info("Seeding initial feed posts...");
            User alex = userRepository.findByUsername("alex_tech").orElse(null);
            if (alex != null) {
                Tweet post1 = new Tweet();
                post1.setAuthorId(alex.getId());
                post1.setAuthorUsername(alex.getUsername());
                post1.setAuthorDisplayName(alex.getDisplayName());
                post1.setContent("Excited to launch our virtual shopfront & commercial fleet on ASMJ Biz Connect! Pick items directly from the shelves into your basket. #ASMJBizConnect #Innovation");
                post1.setLikesCount(14);
                post1.setRepliesCount(2);
                post1.setMediaStatus(MediaStatus.NONE);
                post1.setCreatedAt(LocalDateTime.now().minusDays(1));
                tweetRepository.save(post1);
            }
        }

        // 3. Seed Virtual Store Products
        if (productRepository.count() == 0) {
            log.info("Seeding virtual storefront products on aisle shelves...");

            MarketplaceProduct p1 = MarketplaceProduct.builder()
                    .sellerUsername("alex_tech")
                    .sellerDisplayName("Alex Rivera")
                    .title("Dell UltraSharp 32\" 4K USB-C Hub Monitor")
                    .description("IPS Black technology with 2000:1 contrast ratio, 90W Power Delivery, RJ45 Ethernet, and factory color calibration.")
                    .category(ProductCategory.HARDWARE_AND_TECH)
                    .unitPrice(BigDecimal.valueOf(54999.00))
                    .stockQuantity(12)
                    .shelfAisle("Aisle 1 - Tech Hardware")
                    .badgeTag("BEST SELLER")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .build();

            MarketplaceProduct p2 = MarketplaceProduct.builder()
                    .sellerUsername("sarah_designs")
                    .sellerDisplayName("Sarah Chen")
                    .title("Ergonomic Mesh High-Back Executive Task Chair")
                    .description("3D adjustable lumbar support, 4D armrests, breathable Korean mesh, and heavy-duty synchronized tilt mechanism.")
                    .category(ProductCategory.OFFICE_EQUIPMENT)
                    .unitPrice(BigDecimal.valueOf(18500.00))
                    .stockQuantity(20)
                    .shelfAisle("Aisle 2 - Office Suite")
                    .badgeTag("TOP VALUE")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1580481077197-2c93833b3aef?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            MarketplaceProduct p3 = MarketplaceProduct.builder()
                    .sellerUsername("alex_tech")
                    .sellerDisplayName("Alex Rivera")
                    .title("Enterprise Cloud Infrastructure Audit & Optimization")
                    .description("Comprehensive AWS/GCP architecture security review, FinOps cost reduction analysis, and terraform blueprint delivery.")
                    .category(ProductCategory.PROFESSIONAL_SERVICES)
                    .unitPrice(BigDecimal.valueOf(75000.00))
                    .stockQuantity(5)
                    .shelfAisle("Aisle 3 - B2B Services")
                    .badgeTag("SERVICE")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            productRepository.saveAll(List.of(p1, p2, p3));
        }

        // 4. Seed Vehicles
        if (vehicleListingRepository.count() == 0) {
            log.info("Seeding vehicle rental listings...");

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

            vehicleListingRepository.saveAll(List.of(v1, v2));

            VehicleBooking booking = VehicleBooking.builder()
                    .listingId(v1.getId())
                    .vehicleSummary("2023 Hyundai Creta SX (O)")
                    .renterUsername("john_doe")
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

        // 5. Seed Insurance Ads
        if (insuranceAdRepository.count() == 0) {
            log.info("Seeding insurance catalog...");

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

            insuranceAdRepository.saveAll(List.of(ad1, ad2));
        }

        log.info("Database synchronization check complete.");
    }

    private void createAccountIfMissing(String username, String displayName, String email, String bio, Set<String> roles) {
        String cleanUsername = username.trim().toLowerCase().replaceAll("\\s+", "_");
        if (userRepository.findByUsername(cleanUsername).isEmpty()) {
            User user = new User();
            user.setUsername(cleanUsername);
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setBio(bio);
            user.setLocation("Bengaluru, India");
            user.setRoles(new HashSet<>(roles));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Seeded user account: @{}", cleanUsername);
        }
    }
}