package com.joshi.twitterclone.config;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.DirectMessage;
import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.events.*;
import com.joshi.twitterclone.model.jobs.*;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.DirectMessageRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final ProductServiceRepository productServiceRepository;
    private final JobListingRepository jobListingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final EventListingRepository eventListingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting comprehensive data initialization across all platform domains...");

        seedUsers();
        seedTweets();
        seedConversations();
        seedVehicleRentals();
        seedInsuranceCatalog();
        seedProductsAndServices();
        seedJobListingsAndApplications();
        seedEventsAndBookings();

        log.info("All domain seeders executed successfully.");
    }

    private void seedUsers() {
        createAccountIfMissing("admin", "ASMJ Administrator", "admin@asmjbizconnect.com", 
                "Official platform administrator & marketplace review moderator.", "Bengaluru, India", Set.of("ROLE_USER", "ROLE_ADMIN"));
        
        createAccountIfMissing("alex_tech", "Alex Rivera", "alex@nexuscloud.io", 
                "Cloud Architect & Tech Lead at Nexus Systems. Fleet operator and startup mentor.", "Bengaluru, India", Set.of("ROLE_USER"));
        
        createAccountIfMissing("sarah_designs", "Sarah Chen", "sarah@alliedshield.com", 
                "Corporate Risk Advisor & Insurance Partner at Allied National. SaaS design specialist.", "Mumbai, India", Set.of("ROLE_USER"));
        
        createAccountIfMissing("john_doe", "John Doe", "john@doelogistics.com", 
                "Logistics Director & Commercial Vehicle Operator. Fullstack hobbyist.", "Delhi, India", Set.of("ROLE_USER"));
        
        createAccountIfMissing("priya_hr", "Priya Sharma", "priya@talentbridge.co", 
                "Talent Acquisition Director & Executive Recruiter for hypergrowth tech teams.", "Hyderabad, India", Set.of("ROLE_USER"));
        
        createAccountIfMissing("vikram_fleet", "Vikram Malhotra", "vikram@malhotratrans.com", 
                "Managing Director at Malhotra Mobility Solutions. Commercial EV & SUV fleet partner.", "Pune, India", Set.of("ROLE_USER"));
    }

    private void seedTweets() {
        if (tweetRepository.count() == 0) {
            log.info("Seeding timeline posts...");
            User alex = userRepository.findByUsername("alex_tech").orElse(null);
            User sarah = userRepository.findByUsername("sarah_designs").orElse(null);
            User priya = userRepository.findByUsername("priya_hr").orElse(null);
            User vikram = userRepository.findByUsername("vikram_fleet").orElse(null);

            List<Tweet> tweets = List.of(
                createTweet(alex, "Excited to launch our commercial EV fleet on ASMJ Biz Connect! Inter-city and corporate bookings now live with verified insurance. #Innovation #Mobility #EV"),
                createTweet(sarah, "Just released our 2026 Commercial Fleet Comprehensive Protection plans. Fast cashless settlement across 8,000+ garages. #Insurance #FinTech #FleetShield"),
                createTweet(priya, "We are actively recruiting Senior Spring Boot Architects, DevOps Leads, and Product Designers for our Bengaluru and Mumbai hubs! Apply directly on the Jobs portal. #Hiring #TechJobs #Career"),
                createTweet(vikram, "Added 5 luxury executive sedans and 4x4 off-road SUVs to the rental marketplace for executive travel and corporate events. #CarRental #BusinessTravel"),
                createTweet(alex, "Microservice performance optimization session this Friday at the Bengaluru Tech Summit. Tickets are moving fast on the Events tab! #Cloud #SpringBoot #Architecture"),
                createTweet(sarah, "Our B2B SaaS Design System deliverable package is now available in the Products & Services catalog. Check it out for Figma to Tailwind mapping. #UIUX #DesignSystems")
            );

            tweetRepository.saveAll(tweets);
        }
    }

    private void seedConversations() {
        if (conversationRepository.count() == 0) {
            log.info("Seeding active conversations and messages...");
            User john = userRepository.findByUsername("john_doe").orElse(null);
            User alex = userRepository.findByUsername("alex_tech").orElse(null);

            if (john != null && alex != null) {
                Conversation convo = new Conversation();
                convo.setGroup(false);
                convo.setParticipantUsernames(new HashSet<>(Set.of(john.getUsername(), alex.getUsername())));
                convo.setLastMessage("Confirmed! Looking forward to picking up the Hyundai Creta.");
                convo.setLastSenderName(john.getDisplayName());
                convo.setCreatedAt(LocalDateTime.now().minusDays(2));
                convo.setUpdatedAt(LocalDateTime.now().minusMinutes(30));
                Conversation saved = conversationRepository.save(convo);

                DirectMessage m1 = new DirectMessage();
                m1.setConversationId(saved.getId());
                m1.setSenderId(john.getId());
                m1.setSenderUsername(john.getUsername());
                m1.setSenderDisplayName(john.getDisplayName());
                m1.setContent("Hi Alex, is the Creta available for corporate lease starting tomorrow?");
                m1.setCreatedAt(LocalDateTime.now().minusHours(3));

                DirectMessage m2 = new DirectMessage();
                m2.setConversationId(saved.getId());
                m2.setSenderId(alex.getId());
                m2.setSenderUsername(alex.getUsername());
                m2.setSenderDisplayName(alex.getDisplayName());
                m2.setContent("Yes John, it's fully sanitized, insured, and ready at our Indiranagar hub.");
                m2.setCreatedAt(LocalDateTime.now().minusHours(2));

                DirectMessage m3 = new DirectMessage();
                m3.setConversationId(saved.getId());
                m3.setSenderId(john.getId());
                m3.setSenderUsername(john.getUsername());
                m3.setSenderDisplayName(john.getDisplayName());
                m3.setContent("Confirmed! Looking forward to picking up the Hyundai Creta.");
                m3.setCreatedAt(LocalDateTime.now().minusMinutes(30));

                directMessageRepository.saveAll(List.of(m1, m2, m3));
            }
        }
    }

    private void seedVehicleRentals() {
        if (vehicleListingRepository.count() == 0 || vehicleListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).isEmpty()) {
            log.info("Seeding rental vehicle marketplace listings...");

            VehicleListing v1 = VehicleListing.builder()
                    .ownerUsername("alex_tech")
                    .ownerDisplayName("Alex Rivera")
                    .contactNumber("+91 9876543210")
                    .make("Hyundai")
                    .modelName("Creta SX (O) Turbo")
                    .year(2024)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Petrol")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Bengaluru")
                    .pickupAddress("Indiranagar 100ft Road, Bengaluru")
                    .dailyRentalRate(BigDecimal.valueOf(2800.00))
                    .securityDeposit(BigDecimal.valueOf(5000.00))
                    .description("Top-spec SUV with panoramic sunroof, ventilated leather seats, Bose audio, and ADAS Level 2 safety.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();

            VehicleListing v2 = VehicleListing.builder()
                    .ownerUsername("vikram_fleet")
                    .ownerDisplayName("Vikram Malhotra")
                    .contactNumber("+91 9822334455")
                    .make("Tata")
                    .modelName("Nexon EV Max Long Range")
                    .year(2024)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Electric")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Mumbai")
                    .pickupAddress("BKC Commercial Complex, Bandra East, Mumbai")
                    .dailyRentalRate(BigDecimal.valueOf(3100.00))
                    .securityDeposit(BigDecimal.valueOf(6000.00))
                    .description("Zero-emission executive EV with 450km range, complimentary fast charging RFID pass, and plush cabin.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build();

            VehicleListing v3 = VehicleListing.builder()
                    .ownerUsername("vikram_fleet")
                    .ownerDisplayName("Vikram Malhotra")
                    .contactNumber("+91 9822334455")
                    .make("Toyota")
                    .modelName("Innova HyCross Hybrid")
                    .year(2023)
                    .vehicleType(VehicleType.COMMERCIAL)
                    .fuelType("Hybrid")
                    .transmission("Automatic")
                    .seatingCapacity(7)
                    .locationCity("Bengaluru")
                    .pickupAddress("Airport Road, Yelahanka, Bengaluru")
                    .dailyRentalRate(BigDecimal.valueOf(4500.00))
                    .securityDeposit(BigDecimal.valueOf(8000.00))
                    .description("Premium 7-seater executive lounge MPV with captain seats, ottoman recliners, and massive luggage capacity.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1559416523-140ddc3d238c?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(6))
                    .build();

            VehicleListing v4 = VehicleListing.builder()
                    .ownerUsername("john_doe")
                    .ownerDisplayName("John Doe")
                    .contactNumber("+91 9811223344")
                    .make("Mahindra")
                    .modelName("Thar 4x4 Hard Top")
                    .year(2023)
                    .vehicleType(VehicleType.SUV)
                    .fuelType("Diesel")
                    .transmission("Manual")
                    .seatingCapacity(4)
                    .locationCity("Delhi")
                    .pickupAddress("Connaught Place Outer Circle, New Delhi")
                    .dailyRentalRate(BigDecimal.valueOf(3600.00))
                    .securityDeposit(BigDecimal.valueOf(7000.00))
                    .description("Rugged 4x4 off-road SUV built for executive expeditions, team off-sites, and weekend hill-station travel.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            VehicleListing v5 = VehicleListing.builder()
                    .ownerUsername("alex_tech")
                    .ownerDisplayName("Alex Rivera")
                    .contactNumber("+91 9876543210")
                    .make("BMW")
                    .modelName("3 Series Gran Limousine")
                    .year(2024)
                    .vehicleType(VehicleType.LUXURY)
                    .fuelType("Petrol")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Bengaluru")
                    .pickupAddress("UB City Commercial Hub, Bengaluru")
                    .dailyRentalRate(BigDecimal.valueOf(9500.00))
                    .securityDeposit(BigDecimal.valueOf(25000.00))
                    .description("Chauffeur or self-drive luxury executive sedan with rear comfort lounge, ambient lighting, and Harman Kardon surround.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            VehicleListing v6 = VehicleListing.builder()
                    .ownerUsername("vikram_fleet")
                    .ownerDisplayName("Vikram Malhotra")
                    .contactNumber("+91 9822334455")
                    .make("Maruti Suzuki")
                    .modelName("Swift ZXi Plus")
                    .year(2024)
                    .vehicleType(VehicleType.HATCHBACK)
                    .fuelType("Petrol")
                    .transmission("Automatic")
                    .seatingCapacity(5)
                    .locationCity("Pune")
                    .pickupAddress("Viman Nagar, Pune")
                    .dailyRentalRate(BigDecimal.valueOf(1600.00))
                    .securityDeposit(BigDecimal.valueOf(3000.00))
                    .description("Fuel-efficient city commuter with wireless Apple CarPlay/Android Auto and 360-degree parking cameras.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            VehicleListing v7Pending = VehicleListing.builder()
                    .ownerUsername("john_doe")
                    .ownerDisplayName("John Doe")
                    .contactNumber("+91 9811223344")
                    .make("Force")
                    .modelName("Urbania Luxury Van")
                    .year(2024)
                    .vehicleType(VehicleType.COMMERCIAL)
                    .fuelType("Diesel")
                    .transmission("Manual")
                    .seatingCapacity(12)
                    .locationCity("Delhi")
                    .pickupAddress("Aerocity Corporate District, Delhi")
                    .dailyRentalRate(BigDecimal.valueOf(7500.00))
                    .securityDeposit(BigDecimal.valueOf(15000.00))
                    .description("12-seater corporate shuttle van with individual reclining bucket seats, AC vents for all rows, and ample cargo.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1570125909232-eb263c188f7e?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.PENDING_APPROVAL)
                    .createdAt(LocalDateTime.now().minusHours(3))
                    .build();

            vehicleListingRepository.saveAll(List.of(v1, v2, v3, v4, v5, v6, v7Pending));

            if (vehicleBookingRepository.count() == 0) {
                VehicleBooking booking = VehicleBooking.builder()
                        .listingId(v1.getId())
                        .vehicleSummary("2024 Hyundai Creta SX (O) Turbo")
                        .renterUsername("john_doe")
                        .ownerUsername("alex_tech")
                        .startDate(LocalDate.now().plusDays(2))
                        .endDate(LocalDate.now().plusDays(5))
                        .totalDays(3)
                        .totalAmount(BigDecimal.valueOf(8400.00))
                        .customerPhone("+91 9811223344")
                        .drivingLicenseNumber("DL-1420210098765")
                        .bookingStatus("CONFIRMED")
                        .bookedAt(LocalDateTime.now().minusHours(5))
                        .build();

                vehicleBookingRepository.save(booking);
            }
        }
    }

    private void seedInsuranceCatalog() {
        if (insuranceAdRepository.count() == 0 || insuranceAdRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).isEmpty()) {
            log.info("Seeding insurance advertisement catalog...");

            InsuranceAd ad1 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Allied National General Insurance")
                    .title("Corporate Fleet Complete Shield 360")
                    .insuranceType(InsuranceType.VEHICLE_COMPREHENSIVE)
                    .baseAnnualPremium(BigDecimal.valueOf(15000.00))
                    .coverageAmount(BigDecimal.valueOf(2000000.00))
                    .policyHighlights("Zero-depreciation bumper-to-bumper cover, engine & gearbox protection, tyre cover, and 24/7 pan-India roadside assistance.")
                    .keyBenefits(List.of("Zero Depreciation", "Cashless Garage Network (8000+)", "Instant Survey Via Video Call"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(12))
                    .build();

            InsuranceAd ad2 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Tata AIG Risk Solutions")
                    .title("Enterprise Group Health & Wellness Shield")
                    .insuranceType(InsuranceType.HEALTH_FAMILY)
                    .baseAnnualPremium(BigDecimal.valueOf(24000.00))
                    .coverageAmount(BigDecimal.valueOf(3500000.00))
                    .policyHighlights("Comprehensive group health coverage for business employees & family members with pre-existing disease cover from Day 1.")
                    .keyBenefits(List.of("No Room Rent Capping", "Maternity & Newborn Cover", "OPD Reimbursements"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();

            InsuranceAd ad3 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("HDFC ERGO Enterprise")
                    .title("B2B Tech Cyber Liability & Data Breach Cover")
                    .insuranceType(InsuranceType.BUSINESS_LIABILITY)
                    .baseAnnualPremium(BigDecimal.valueOf(38000.00))
                    .coverageAmount(BigDecimal.valueOf(10000000.00))
                    .policyHighlights("Financial protection against ransomware recovery, business interruption, regulatory penalties, and legal defense expenses.")
                    .keyBenefits(List.of("Global Jurisdiction", "Forensic Investigation Costs", "Ransomware Negotiation & Restoration"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(7))
                    .build();

            InsuranceAd ad4 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("ICICI Lombard General")
                    .title("Commercial Goods Carrier & Transit Policy")
                    .insuranceType(InsuranceType.VEHICLE_THIRD_PARTY)
                    .baseAnnualPremium(BigDecimal.valueOf(11500.00))
                    .coverageAmount(BigDecimal.valueOf(1500000.00))
                    .policyHighlights("Mandatory statutory 3rd party liability cover combined with cargo protection against accident and fire damages during transit.")
                    .keyBenefits(List.of("Unlimited 3rd Party Injury Cover", "Cargo Loss Settlement", "Quick Digital Claim Processing"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            InsuranceAd ad5 = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Max Life Corporate Solutions")
                    .title("Keyman & Executive Term Life Security")
                    .insuranceType(InsuranceType.TERM_LIFE)
                    .baseAnnualPremium(BigDecimal.valueOf(18500.00))
                    .coverageAmount(BigDecimal.valueOf(25000000.00))
                    .policyHighlights("Safeguard business continuity against unforeseen loss of key executive personnel with tax-efficient corporate premium structures.")
                    .keyBenefits(List.of("High Sum Assured Multiple", "Terminal Illness Acceleration", "Business Loan Cover Extension"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            InsuranceAd ad6Pending = InsuranceAd.builder()
                    .insurerUsername("sarah_designs")
                    .insurerDisplayName("Sarah Chen")
                    .providerCompany("Bajaj Allianz Risk")
                    .title("Professional Indemnity & Errors & Omissions")
                    .insuranceType(InsuranceType.BUSINESS_LIABILITY)
                    .baseAnnualPremium(BigDecimal.valueOf(29000.00))
                    .coverageAmount(BigDecimal.valueOf(5000000.00))
                    .policyHighlights("Protection against negligence claims, client financial loss damages, and intellectual property defense costs.")
                    .keyBenefits(List.of("Contractual Liability Cover", "Out of Court Settlement Assistance", "Retroactive Date Benefits"))
                    .status(ListingStatus.PENDING_APPROVAL)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .build();

            insuranceAdRepository.saveAll(List.of(ad1, ad2, ad3, ad4, ad5, ad6Pending));

            if (insuranceQuoteRepository.count() == 0) {
                InsuranceQuote quote = InsuranceQuote.builder()
                        .adId(ad1.getId())
                        .adTitle(ad1.getTitle())
                        .insurerUsername("sarah_designs")
                        .applicantUsername("john_doe")
                        .applicantName("John Doe")
                        .applicantEmail("john@doelogistics.com")
                        .applicantPhone("+91 9811223344")
                        .applicantAge(35)
                        .estimatedValueOrSumInsured(BigDecimal.valueOf(1400000.00))
                        .calculatedQuotePremium(BigDecimal.valueOf(49000.00))
                        .status("GENERATED")
                        .requestedAt(LocalDateTime.now().minusHours(4))
                        .build();

                insuranceQuoteRepository.save(quote);
            }
        }
    }

    private void seedProductsAndServices() {
        if (productServiceRepository.count() == 0 || productServiceRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).isEmpty()) {
            log.info("Seeding products & services catalog...");

            ProductServiceItem p1 = ProductServiceItem.builder()
                    .vendorUsername("alex_tech")
                    .vendorDisplayName("Alex Rivera")
                    .businessName("Nexus Cloud Systems")
                    .contactNumber("+91 9876543210")
                    .title("Spring Boot Microservices & Cloud Native Architecture Consulting")
                    .category(ItemCategory.SERVICE_TECHNICAL)
                    .price(BigDecimal.valueOf(25000.00))
                    .priceUnit("per sprint")
                    .locationCity("Bengaluru")
                    .deliveryTerms("Remote architectural review, high-concurrency benchmarks, and CI/CD blueprints")
                    .description("Full architectural audit, database sharding strategies on MongoDB, and implementation of high-throughput WebSocket message brokers.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();

            ProductServiceItem p2 = ProductServiceItem.builder()
                    .vendorUsername("sarah_designs")
                    .vendorDisplayName("Sarah Chen")
                    .businessName("Allied Design Studio")
                    .contactNumber("+91 9123456780")
                    .title("Enterprise B2B SaaS Design System & UI Token Kit")
                    .category(ItemCategory.PRODUCT_DIGITAL)
                    .price(BigDecimal.valueOf(14999.00))
                    .priceUnit("commercial license")
                    .locationCity("Mumbai")
                    .deliveryTerms("Instant Figma library access with Tailwind CSS component mappings")
                    .description("120+ accessible UI components, dark-mode native tokens, financial charts, and responsive layouts built for SaaS applications.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build();

            ProductServiceItem p3 = ProductServiceItem.builder()
                    .vendorUsername("john_doe")
                    .vendorDisplayName("John Doe")
                    .businessName("Doe Logistics Network")
                    .contactNumber("+91 9811223344")
                    .title("Dedicated Inter-City Cold Chain Logistics & Freight Handling")
                    .category(ItemCategory.SERVICE_LOGISTICS)
                    .price(BigDecimal.valueOf(18000.00))
                    .priceUnit("per trip")
                    .locationCity("Delhi")
                    .deliveryTerms("GPS-monitored refrigerated transport with real-time temperature telemetry")
                    .description("Temperature-controlled logistics across Delhi-NCR, Mumbai, and Bengaluru corridors with guaranteed same-day dispatch.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(6))
                    .build();

            ProductServiceItem p4 = ProductServiceItem.builder()
                    .vendorUsername("alex_tech")
                    .vendorDisplayName("Alex Rivera")
                    .businessName("Nexus Hardware Labs")
                    .contactNumber("+91 9876543210")
                    .title("1U Enterprise Rack Server (AMD EPYC 64-Core, 256GB ECC RAM, 4TB NVMe)")
                    .category(ItemCategory.PRODUCT_PHYSICAL)
                    .price(BigDecimal.valueOf(320000.00))
                    .priceUnit("per unit")
                    .locationCity("Bengaluru")
                    .deliveryTerms("Insured courier dispatch with 3-year on-site replacement warranty")
                    .description("High-density compute rack server tailored for local virtualization, MongoDB replica sets, and low-latency API workloads.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            ProductServiceItem p5 = ProductServiceItem.builder()
                    .vendorUsername("sarah_designs")
                    .vendorDisplayName("Sarah Chen")
                    .businessName("Allied Compliance Advisory")
                    .contactNumber("+91 9123456780")
                    .title("SOC2, ISO 27001 & Data Privacy Regulatory Audit Service")
                    .category(ItemCategory.SERVICE_CONSULTING)
                    .price(BigDecimal.valueOf(45000.00))
                    .priceUnit("flat audit fee")
                    .locationCity("Mumbai")
                    .deliveryTerms("Gap analysis report, policy templates, and external auditor coordination")
                    .description("Comprehensive compliance readiness preparation for fintech and enterprise startups seeking SOC2 Type II certification.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            ProductServiceItem p6Pending = ProductServiceItem.builder()
                    .vendorUsername("vikram_fleet")
                    .vendorDisplayName("Vikram Malhotra")
                    .businessName("Malhotra Fleet Maintenance")
                    .contactNumber("+91 9822334455")
                    .title("Corporate Commercial Vehicle Preventive Maintenance & AMC")
                    .category(ItemCategory.SERVICE_MAINTENANCE)
                    .price(BigDecimal.valueOf(12000.00))
                    .priceUnit("per vehicle / year")
                    .locationCity("Pune")
                    .deliveryTerms("Quarterly on-site inspection, fluid changes, brake checks, and OBD diagnostics")
                    .description("Scheduled preventive maintenance package for commercial vehicle fleets to maximize uptime and lower fuel consumption.")
                    .imageUrls(List.of("https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80"))
                    .status(ListingStatus.PENDING_APPROVAL)
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .build();

            productServiceRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6Pending));
        }
    }

    private void seedJobListingsAndApplications() {
        if (jobListingRepository.count() == 0 || jobListingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).isEmpty()) {
            log.info("Seeding career opportunities and job applications...");

            JobListing j1 = JobListing.builder()
                    .posterUsername("alex_tech")
                    .posterDisplayName("Alex Rivera")
                    .companyName("Nexus Cloud Systems")
                    .jobTitle("Lead Backend Engineer (Java & Spring Boot)")
                    .employmentType(EmploymentType.FULL_TIME)
                    .workplaceType(WorkplaceType.HYBRID)
                    .locationCity("Bengaluru")
                    .minSalary(BigDecimal.valueOf(2200000.00))
                    .maxSalary(BigDecimal.valueOf(3200000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Lead the backend engineering division building scalable microservices, WebSocket streams, and MongoDB database clusters.")
                    .requirements("5+ years building backend systems in Java, Spring Boot 3, MongoDB, Docker, and distributed caching.")
                    .requiredSkills(List.of("Java", "Spring Boot", "MongoDB", "WebSockets", "Docker"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(1)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();

            JobListing j2 = JobListing.builder()
                    .posterUsername("sarah_designs")
                    .posterDisplayName("Sarah Chen")
                    .companyName("Allied FinTech Lab")
                    .jobTitle("Principal UI/UX Product Designer")
                    .employmentType(EmploymentType.FULL_TIME)
                    .workplaceType(WorkplaceType.REMOTE)
                    .locationCity("Mumbai")
                    .minSalary(BigDecimal.valueOf(1800000.00))
                    .maxSalary(BigDecimal.valueOf(2600000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Shape the next generation of B2B insurance portals, quote calculation workflows, and responsive design systems.")
                    .requirements("Strong portfolio demonstrating responsive design systems, complex data table UX, Figma expertise, and user research.")
                    .requiredSkills(List.of("Figma", "Design Systems", "User Research", "Tailwind CSS"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(0)
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build();

            JobListing j3 = JobListing.builder()
                    .posterUsername("priya_hr")
                    .posterDisplayName("Priya Sharma")
                    .companyName("ScaleUp Cloud Solutions")
                    .jobTitle("DevOps & Site Reliability Engineer (Kubernetes/AWS)")
                    .employmentType(EmploymentType.FULL_TIME)
                    .workplaceType(WorkplaceType.ON_SITE)
                    .locationCity("Hyderabad")
                    .minSalary(BigDecimal.valueOf(1900000.00))
                    .maxSalary(BigDecimal.valueOf(2800000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Manage multi-region Kubernetes clusters, Prometheus/Grafana observability dashboards, and zero-downtime CI/CD pipelines.")
                    .requirements("3+ years in Terraform, Kubernetes, AWS EKS, Helm, and Linux performance tuning.")
                    .requiredSkills(List.of("Kubernetes", "AWS", "Terraform", "CI/CD", "Linux"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(0)
                    .createdAt(LocalDateTime.now().minusDays(6))
                    .build();

            JobListing j4 = JobListing.builder()
                    .posterUsername("priya_hr")
                    .posterDisplayName("Priya Sharma")
                    .companyName("FinServe Global")
                    .jobTitle("Senior Fullstack Developer (React & Spring Boot)")
                    .employmentType(EmploymentType.FULL_TIME)
                    .workplaceType(WorkplaceType.HYBRID)
                    .locationCity("Bengaluru")
                    .minSalary(BigDecimal.valueOf(1600000.00))
                    .maxSalary(BigDecimal.valueOf(2400000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Build responsive web applications integrating Spring Boot REST APIs, real-time message streams, and modern React interfaces.")
                    .requirements("3+ years experience with React, TypeScript, Spring Boot, and relational/NoSQL databases.")
                    .requiredSkills(List.of("React", "TypeScript", "Spring Boot", "REST APIs"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(0)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            JobListing j5 = JobListing.builder()
                    .posterUsername("john_doe")
                    .posterDisplayName("John Doe")
                    .companyName("Doe Logistics Network")
                    .jobTitle("Fleet Operations & Logistics Operations Manager")
                    .employmentType(EmploymentType.FULL_TIME)
                    .workplaceType(WorkplaceType.ON_SITE)
                    .locationCity("Delhi")
                    .minSalary(BigDecimal.valueOf(1100000.00))
                    .maxSalary(BigDecimal.valueOf(1600000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Oversee dispatch logistics, driver route optimization, maintenance scheduling, and corporate client SLA delivery.")
                    .requirements("Degree in Supply Chain or Logistics with 3+ years experience managing multi-city vehicle fleets.")
                    .requiredSkills(List.of("Fleet Management", "Supply Chain", "Route Optimization", "Vendor Relations"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(0)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            JobListing j6 = JobListing.builder()
                    .posterUsername("alex_tech")
                    .posterDisplayName("Alex Rivera")
                    .companyName("Nexus Cloud Systems")
                    .jobTitle("QA Automation Engineer (Selenium & Postman)")
                    .employmentType(EmploymentType.CONTRACT)
                    .workplaceType(WorkplaceType.REMOTE)
                    .locationCity("Bengaluru")
                    .minSalary(BigDecimal.valueOf(900000.00))
                    .maxSalary(BigDecimal.valueOf(1400000.00))
                    .salaryCurrency("INR")
                    .jobDescription("Author automated integration tests, API performance test suites, and load test scripts for high-traffic platforms.")
                    .requirements("2+ years experience in automated testing, JUnit, Selenium, JMeter, and REST Assured.")
                    .requiredSkills(List.of("QA Automation", "Selenium", "Postman", "JUnit", "JMeter"))
                    .status(ListingStatus.APPROVED)
                    .applicantCount(0)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            jobListingRepository.saveAll(List.of(j1, j2, j3, j4, j5, j6));

            if (jobApplicationRepository.count() == 0) {
                JobApplication app = JobApplication.builder()
                        .jobId(j1.getId())
                        .jobTitle(j1.getJobTitle())
                        .companyName(j1.getCompanyName())
                        .posterUsername(j1.getPosterUsername())
                        .applicantUsername("john_doe")
                        .applicantFullName("John Doe")
                        .applicantEmail("john@doelogistics.com")
                        .applicantPhone("+91 9811223344")
                        .yearsOfExperience(6)
                        .coverLetterNote("I have over 6 years of experience building high-throughput Spring Boot backends and managing distributed databases.")
                        .status(ApplicationStatus.SHORTLISTED)
                        .appliedAt(LocalDateTime.now().minusHours(6))
                        .build();

                jobApplicationRepository.save(app);
            }
        }
    }

    private void seedEventsAndBookings() {
        if (eventListingRepository.count() == 0 || eventListingRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(ListingStatus.APPROVED, LocalDate.now()).isEmpty()) {
            log.info("Seeding corporate events catalog and reservations...");

            EventListing e1 = EventListing.builder()
                    .organizerUsername("alex_tech")
                    .organizerDisplayName("Alex Rivera")
                    .organizationName("Bengaluru Tech Forum")
                    .title("Enterprise Spring Boot & Cloud Summit 2026")
                    .eventType(EventType.TECH_CONFERENCE)
                    .eventFormat(EventFormat.IN_PERSON)
                    .eventDate(LocalDate.now().plusDays(6))
                    .startTime(LocalTime.of(9, 30))
                    .endTime(LocalTime.of(17, 30))
                    .venueLocation("NASSCOM Convention Center, Outer Ring Road")
                    .city("Bengaluru")
                    .ticketPrice(BigDecimal.valueOf(999.00))
                    .totalCapacity(200)
                    .bookedSeats(1)
                    .description("Keynote addresses from leading architects on cloud scalability, event-driven microservices, and real-time system design.")
                    .bannerImageUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=800&q=80")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build();

            EventListing e2 = EventListing.builder()
                    .organizerUsername("sarah_designs")
                    .organizerDisplayName("Sarah Chen")
                    .organizationName("FinTech Leaders Guild")
                    .title("B2B Digital Insurance & Risk Technology Expo")
                    .eventType(EventType.EXPO_TRADE_FAIR)
                    .eventFormat(EventFormat.HYBRID)
                    .eventDate(LocalDate.now().plusDays(12))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(16, 0))
                    .venueLocation("Jio World Convention Centre, BKC")
                    .city("Mumbai")
                    .ticketPrice(BigDecimal.ZERO)
                    .totalCapacity(350)
                    .bookedSeats(0)
                    .description("Explore next-generation insurance APIs, digital underwriting workflows, and automated claims settlement architectures.")
                    .bannerImageUrl("https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=800&q=80")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(6))
                    .build();

            EventListing e3 = EventListing.builder()
                    .organizerUsername("priya_hr")
                    .organizerDisplayName("Priya Sharma")
                    .organizationName("TalentBridge Network")
                    .title("Tech Talent & Leadership Hiring Conclave")
                    .eventType(EventType.BUSINESS_NETWORKING)
                    .eventFormat(EventFormat.IN_PERSON)
                    .eventDate(LocalDate.now().plusDays(18))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(19, 0))
                    .venueLocation("HICC Novotel Convention Center")
                    .city("Hyderabad")
                    .ticketPrice(BigDecimal.valueOf(499.00))
                    .totalCapacity(150)
                    .bookedSeats(0)
                    .description("Connect with technology founders, engineering VPs, and hiring managers building high-performing engineering squads.")
                    .bannerImageUrl("https://images.unsplash.com/photo-1515187029135-18ee286d815b?auto=format&fit=crop&w=800&q=80")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build();

            EventListing e4 = EventListing.builder()
                    .organizerUsername("john_doe")
                    .organizerDisplayName("John Doe")
                    .organizationName("Supply Chain Innovation Hub")
                    .title("Green Fleet & EV Logistics Workshop")
                    .eventType(EventType.WORKSHOP_SEMINAR)
                    .eventFormat(EventFormat.ONLINE)
                    .eventDate(LocalDate.now().plusDays(24))
                    .startTime(LocalTime.of(15, 0))
                    .endTime(LocalTime.of(18, 0))
                    .venueLocation("Virtual Zoom Live Stream")
                    .city("Online")
                    .ticketPrice(BigDecimal.ZERO)
                    .totalCapacity(500)
                    .bookedSeats(0)
                    .description("Hands-on strategy masterclass on fleet electrification, Total Cost of Ownership (TCO) optimization, and charging networks.")
                    .bannerImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();

            EventListing e5 = EventListing.builder()
                    .organizerUsername("alex_tech")
                    .organizerDisplayName("Alex Rivera")
                    .organizationName("Bengaluru Founders Club")
                    .title("Founders & Angel Investors Networking Mixer")
                    .eventType(EventType.COMMUNITY_MEETUP)
                    .eventFormat(EventFormat.IN_PERSON)
                    .eventDate(LocalDate.now().plusDays(30))
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(21, 30))
                    .venueLocation("WeWork Galaxy, Residency Road")
                    .city("Bengaluru")
                    .ticketPrice(BigDecimal.valueOf(750.00))
                    .totalCapacity(100)
                    .bookedSeats(0)
                    .description("Curated evening of peer networking, product demos, and discussions with early-stage venture capital investors.")
                    .bannerImageUrl("https://images.unsplash.com/photo-1528605248644-14dd04022da1?auto=format&fit=crop&w=800&q=80")
                    .status(ListingStatus.APPROVED)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            eventListingRepository.saveAll(List.of(e1, e2, e3, e4, e5));
        }
    }

    private void createAccountIfMissing(String username, String displayName, String email, String bio, String location, Set<String> roles) {
        var existingUserOpt = userRepository.findByUsername(username);
        if (existingUserOpt.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setBio(bio);
            user.setLocation(location);
            user.setRoles(new HashSet<>(roles));
            user.setCreatedAt(LocalDateTime.now().minusMonths(3));
            userRepository.save(user);
            log.info("Created user account: @{}", username);
        } else {
            User existingUser = existingUserOpt.get();
            if (existingUser.getRoles() == null || !existingUser.getRoles().containsAll(roles)) {
                if (existingUser.getRoles() == null) {
                    existingUser.setRoles(new HashSet<>());
                }
                existingUser.getRoles().addAll(roles);
                userRepository.save(existingUser);
                log.info("Updated role authorities for user: @{} -> {}", username, existingUser.getRoles());
            }
        }
    }

    private Tweet createTweet(User author, String content) {
        Tweet t = new Tweet();
        if (author != null) {
            t.setAuthorId(author.getId());
            t.setAuthorUsername(author.getUsername());
            t.setAuthorDisplayName(author.getDisplayName());
            t.setAuthorAvatarUrl(author.getAvatarUrl());
        }
        t.setContent(content);
        t.setLikesCount((int) (Math.random() * 15) + 3);
        t.setRepliesCount((int) (Math.random() * 4));
        t.setMediaStatus(MediaStatus.NONE);
        t.setCreatedAt(LocalDateTime.now().minusHours((long) (Math.random() * 48) + 1));
        return t;
    }
}