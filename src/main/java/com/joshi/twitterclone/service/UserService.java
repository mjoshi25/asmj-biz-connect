package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
import com.joshi.twitterclone.dto.RegisterRequest;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
        user.setFollowingUsernames(new HashSet<>());
        user.setFollowerUsernames(new HashSet<>());

        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String clean = username.trim();
        return userRepository.findByUsername(clean.toLowerCase())
                .or(() -> userRepository.findByUsername(clean))
                .orElse(null);
    }

    public ProfileDto getProfile(String targetUsername, String currentUsername) {
        User targetUser = getUserByUsername(targetUsername);
        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User @" + targetUsername + " not found");
        }

        boolean isSelf = currentUsername != null && targetUser.getUsername().equalsIgnoreCase(currentUsername.trim());

        boolean isFollowing = false;
        if (!isSelf && currentUsername != null) {
            User currentUser = getUserByUsername(currentUsername);
            if (currentUser != null && currentUser.getFollowingUsernames() != null) {
                isFollowing = currentUser.getFollowingUsernames().contains(targetUser.getUsername());
            }
        }

        List<Tweet> userTweets = tweetRepository.findByAuthorIdOrderByCreatedAtDesc(targetUser.getId());

        int followersCount = targetUser.getFollowerUsernames() != null ? targetUser.getFollowerUsernames().size() : 0;
        int followingCount = targetUser.getFollowingUsernames() != null ? targetUser.getFollowingUsernames().size() : 0;

        return ProfileDto.builder()
                .user(targetUser)
                .isSelf(isSelf)
                .isFollowing(isFollowing)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .tweetsCount(userTweets != null ? userTweets.size() : 0)
                .tweets(userTweets != null ? userTweets : List.of())
                .build();
    }

    public boolean toggleFollow(String targetUsername, String currentUsername) {
        if (targetUsername == null || currentUsername == null) {
            return false;
        }

        String cleanTarget = targetUsername.trim().toLowerCase();
        String cleanCurrent = currentUsername.trim().toLowerCase();

        if (cleanTarget.equalsIgnoreCase(cleanCurrent)) {
            return false;
        }

        User targetUser = getUserByUsername(cleanTarget);
        User currentUser = getUserByUsername(cleanCurrent);

        if (targetUser == null || currentUser == null) {
            log.warn("Cannot toggle follow: target ({}) or current ({}) user not found", targetUsername, currentUsername);
            return false;
        }

        if (currentUser.getFollowingUsernames() == null) {
            currentUser.setFollowingUsernames(new HashSet<>());
        }
        if (targetUser.getFollowerUsernames() == null) {
            targetUser.setFollowerUsernames(new HashSet<>());
        }

        boolean isNowFollowing;
        if (currentUser.getFollowingUsernames().contains(targetUser.getUsername())) {
            currentUser.getFollowingUsernames().remove(targetUser.getUsername());
            targetUser.getFollowerUsernames().remove(currentUser.getUsername());
            isNowFollowing = false;
        } else {
            currentUser.getFollowingUsernames().add(targetUser.getUsername());
            targetUser.getFollowerUsernames().add(currentUser.getUsername());
            isNowFollowing = true;
        }

        userRepository.save(currentUser);
        userRepository.save(targetUser);

        return isNowFollowing;
    }

    public User updateProfile(String username, EditProfileRequest request) {
        User user = getUserByUsername(username);
        if (user == null) return null;

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

    public List<User> getSuggestedUsersToFollow(String currentUsername, int limit) {
        String cleanCurrent = currentUsername != null ? currentUsername.trim().toLowerCase() : "";
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equalsIgnoreCase(cleanCurrent))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}