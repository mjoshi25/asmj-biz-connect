package com.joshi.twitterclone.config;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial platform users for ASMJ Biz Connect...");

            User admin = new User();
            admin.setUsername("admin");
            admin.setDisplayName("ASMJ Administrator");
            admin.setEmail("admin@asmjbizconnect.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setBio("Official ASMJ Platform & Ad Moderation Admin.");
            admin.setLocation("San Francisco, CA");
            admin.setRoles(new HashSet<>(Set.of("ROLE_USER", "ROLE_ADMIN")));
            admin.setCreatedAt(LocalDateTime.now());

            User alex = new User();
            alex.setUsername("alex_tech");
            alex.setDisplayName("Alex Rivera");
            alex.setEmail("alex@example.com");
            alex.setPassword(passwordEncoder.encode("password123"));
            alex.setBio("Senior Backend Engineer building distributed systems & WebSocket pipelines.");
            alex.setLocation("Bengaluru, India");
            alex.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            alex.setCreatedAt(LocalDateTime.now());

            User sarah = new User();
            sarah.setUsername("sarah_designs");
            sarah.setDisplayName("Sarah Chen");
            sarah.setEmail("sarah@example.com");
            sarah.setPassword(passwordEncoder.encode("password123"));
            sarah.setBio("Product Designer & UI/UX Architect creating clean enterprise workflows.");
            sarah.setLocation("Singapore");
            sarah.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            sarah.setCreatedAt(LocalDateTime.now());

            User gadgetHub = new User();
            gadgetHub.setUsername("gadget_hub");
            gadgetHub.setDisplayName("Gadget Hub Global");
            gadgetHub.setEmail("biz@gadgethub.com");
            gadgetHub.setPassword(passwordEncoder.encode("password123"));
            gadgetHub.setBio("Verified Merchant | Enterprise Hardware & Developer Gear.");
            gadgetHub.setLocation("Singapore");
            gadgetHub.setRoles(new HashSet<>(Set.of("ROLE_USER")));
            gadgetHub.setCreatedAt(LocalDateTime.now());

            // Build initial network connections
            alex.getFollowing().add("sarah_designs");
            alex.getFollowing().add("gadget_hub");
            sarah.getFollowers().add("alex_tech");
            gadgetHub.getFollowers().add("alex_tech");

            sarah.getFollowing().add("alex_tech");
            alex.getFollowers().add("sarah_designs");

            userRepository.save(admin);
            userRepository.save(alex);
            userRepository.save(sarah);
            userRepository.save(gadgetHub);

            log.info("Successfully seeded 4 default accounts (admin, alex_tech, sarah_designs, gadget_hub).");
        }
    }
}