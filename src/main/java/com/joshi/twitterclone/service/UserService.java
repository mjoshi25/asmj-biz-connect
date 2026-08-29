package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
import com.joshi.twitterclone.dto.RegisterRequest;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public boolean usernameExists(String username) {
        if (username == null || username.isBlank()) return false;
        return userRepository.findByUsername(username.toLowerCase().trim()).isPresent();
    }

    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.findByEmail(email.toLowerCase().trim()).isPresent();
    }

    public User registerUser(RegisterRequest request) {
        if (usernameExists(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }
        if (emailExists(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername().toLowerCase().trim());
        user.setDisplayName(request.getDisplayName() != null && !request.getDisplayName().isBlank() 
                ? request.getDisplayName().trim() 
                : request.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User @" + username + " not found"));
    }

    public ProfileDto getProfile(String targetUsername, String currentUsername) {
        User targetUser = getUserByUsername(targetUsername);
        boolean isSelf = targetUser.getUsername().equalsIgnoreCase(currentUsername);

        List<Tweet> userTweets = tweetRepository.findByAuthorIdOrderByCreatedAtDesc(targetUser.getId());

        return ProfileDto.builder()
                .user(targetUser)
                .isSelf(isSelf)
                .tweetsCount(userTweets.size())
                .tweets(userTweets)
                .build();
    }

    public User updateProfile(String username, EditProfileRequest request) {
        User user = getUserByUsername(username);

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite().trim());
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = fileStorageService.saveImageOptimized(request.getAvatar());
            user.setAvatarUrl(avatarUrl);
        }

        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            String bannerUrl = fileStorageService.saveImageOptimized(request.getBanner());
            user.setBannerUrl(bannerUrl);
        }

        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public void updateUserPrivacyPreferences(String username, boolean isPrivateAccount, boolean showEmailToPublic, boolean allowDirectMessagesFromEveryone) {
        User user = getUserByUsername(username);
        user.setPrivateAccount(isPrivateAccount);
        user.setShowEmailToPublic(showEmailToPublic);
        user.setAllowDirectMessagesFromEveryone(allowDirectMessagesFromEveryone);
        userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void toggleUserStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}