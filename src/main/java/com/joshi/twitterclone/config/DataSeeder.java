package com.joshi.twitterclone.config;

import com.joshi.twitterclone.model.Advertisement;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.AdvertisementRepository;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    private final AdvertisementRepository adRepository;
    private final ConversationRepository conversationRepository;
    private final DirectMessageService directMessageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Running DataSeeder verification check...");

        // 1. Ensure Admin User Exists with ROLE_ADMIN
        User admin = upsertUser(
                "admin",
                "Admin Officer",
                "admin@nexus.local",
                "password123",
                "Platform Administrator & Compliance Lead",
                Set.of("ROLE_USER", "ROLE_ADMIN")
        );

        // 2. Ensure Mock Demo Users Exist
        User alex = upsertUser(
                "alex_tech",
                "Alex Rivera",
                "alex@nexus.local",
                "password123",
                "Full-stack engineer & open-source enthusiast 🚀 #Java #SpringBoot",
                Set.of("ROLE_USER")
        );

        User sarah = upsertUser(
                "sarah_designs",
                "Sarah Jenkins",
                "sarah@nexus.local",
                "password123",
                "UI/UX Designer crafting minimal dark interfaces ✨",
                Set.of("ROLE_USER")
        );

        User gadgetHub = upsertUser(
                "gadget_hub",
                "GadgetHub Official",
                "sponsor@gadgethub.io",
                "password123",
                "Next-gen mechanical keyboards and workspace gear.",
                Set.of("ROLE_USER")
        );

        // Mutual follow relationships
        if (!alex.getFollowing().contains(sarah.getId())) {
            alex.getFollowing().add(sarah.getId());
            sarah.getFollowers().add(alex.getId());
            alex.getFollowing().add(gadgetHub.getId());
            gadgetHub.getFollowers().add(alex.getId());
            userRepository.saveAll(List.of(alex, sarah, gadgetHub));
        }

        // 3. Seed Sample Tweets if collection is empty
        if (tweetRepository.count() == 0) {
            createTweet(alex, "Just migrated our entire backend messaging pipeline to Spring Boot 3 + STOMP WebSockets. The real-time throughput is incredible! 🔥 #SpringBoot #WebDev");
            createTweet(sarah, "Redesigning the dark mode palette for Nexus today. Working with deep zinc and subtle violet gradients. Thoughts? 🎨 #Design");
            createTweet(gadgetHub, "Excited to launch our new ergonomic workspace gear on the Nexus Marketplace! Check out our active promotions in the marketplace tab.");
            log.info("Seeded sample tweets.");
        }

        // 4. Seed Sample Advertisements if collection is empty
        if (adRepository.count() == 0) {
            // Approved Ad
            Advertisement approvedAd = new Advertisement();
            approvedAd.setAdvertiserId(gadgetHub.getId());
            approvedAd.setAdvertiserUsername(gadgetHub.getUsername());
            approvedAd.setAdvertiserDisplayName(gadgetHub.getDisplayName());
            approvedAd.setTitle("🚀 40% Off Keychron Custom Keyboards");
            approvedAd.setDescription("Upgrade your desk setup with hot-swappable mechanical keyboards and customized PBT keycaps. Limited stock available!");
            approvedAd.setTargetUrl("https://example.com/keychron-deal");
            approvedAd.setBudget(new BigDecimal("500.00"));
            approvedAd.setStatus("APPROVED");
            approvedAd.setReviewedByAdminUsername("admin");
            approvedAd.setReviewedAt(LocalDateTime.now().minusDays(1));
            approvedAd.setStartsAt(LocalDateTime.now().minusDays(1));
            approvedAd.setExpiresAt(LocalDateTime.now().plusDays(29));
            adRepository.save(approvedAd);

            // Pending Approval Ad
            Advertisement pendingAd = new Advertisement();
            pendingAd.setAdvertiserId(alex.getId());
            pendingAd.setAdvertiserUsername(alex.getUsername());
            pendingAd.setAdvertiserDisplayName(alex.getDisplayName());
            pendingAd.setTitle("⚡ Fast Cloud Hosting - 3 Months Free");
            pendingAd.setDescription("Deploy your Java Spring microservices with automated CI/CD and zero-config SSL certificates.");
            pendingAd.setTargetUrl("https://example.com/cloud-free-tier");
            pendingAd.setBudget(new BigDecimal("250.00"));
            pendingAd.setStatus("PENDING_APPROVAL");
            adRepository.save(pendingAd);

            log.info("Seeded sample approved and pending advertisements.");
        }

        // 5. Seed Direct Messages if empty
        if (conversationRepository.count() == 0) {
            Conversation alexSarahConvo = directMessageService.getOrCreateDirectConversation(alex.getUsername(), sarah.getUsername());
            directMessageService.sendMessage(alex.getUsername(), alexSarahConvo.getId(), "Hey Sarah! Have you checked out the new group chat and file sharing?", null);
            directMessageService.sendMessage(sarah.getUsername(), alexSarahConvo.getId(), "Yes! Just tested uploading attachments, working smoothly!", null);
            log.info("Seeded initial conversations.");
        }

        log.info("DataSeeder completed. Admin credentials -> Username: 'admin' | Password: 'password123'");
    }

    private User upsertUser(String username, String displayName, String email, String rawPassword, String bio, Set<String> roles) {
        return userRepository.findByUsername(username.toLowerCase()).map(existing -> {
            boolean updated = false;
            if (existing.getRoles() == null || !existing.getRoles().containsAll(roles)) {
                existing.setRoles(new HashSet<>(roles));
                updated = true;
            }
            return updated ? userRepository.save(existing) : existing;
        }).orElseGet(() -> {
            User u = new User();
            u.setUsername(username.toLowerCase());
            u.setDisplayName(displayName);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setBio(bio);
            u.setRoles(new HashSet<>(roles));
            log.info("Created user: {}", username);
            return userRepository.save(u);
        });
    }

    private void createTweet(User author, String content) {
        Tweet t = new Tweet();
        t.setAuthorId(author.getId());
        t.setAuthorUsername(author.getUsername());
        t.setAuthorDisplayName(author.getDisplayName());
        t.setContent(content);
        t.setCreatedAt(LocalDateTime.now().minusHours(2));
        tweetRepository.save(t);
    }
}