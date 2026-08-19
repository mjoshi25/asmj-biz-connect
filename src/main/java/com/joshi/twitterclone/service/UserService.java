package com.joshi.twitterclone.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.dto.EditProfileRequest;
import com.joshi.twitterclone.dto.ProfileDto;
import com.joshi.twitterclone.dto.RegisterRequest;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username.toLowerCase().trim()).isPresent();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim()).isPresent();
    }

    public void registerUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername().toLowerCase().trim());
        user.setDisplayName(request.getDisplayName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public ProfileDto getProfile(String targetUsername, String currentUsername) {
        User targetUser = userRepository.findByUsername(targetUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User @" + targetUsername + " not found"));
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        List<Tweet> tweets = tweetRepository.findByAuthorIdOrderByCreatedAtDesc(targetUser.getId());

        boolean isSelf = targetUser.getId().equals(currentUser.getId());
        boolean isFollowing = currentUser.getFollowing().contains(targetUser.getId());

        return ProfileDto.builder()
                .user(targetUser)
                .tweets(tweets)
                .tweetCount(tweets.size())
                .followerCount(targetUser.getFollowers().size())
                .followingCount(targetUser.getFollowing().size())
                .isSelf(isSelf)
                .isFollowing(isFollowing)
                .build();
    }

    public void toggleFollow(String currentUsername, String targetUsername) {
        if (currentUsername.equalsIgnoreCase(targetUsername)) {
            return;
        }

        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));
        User targetUser = userRepository.findByUsername(targetUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        String targetId = targetUser.getId();
        String currentId = currentUser.getId();

        if (currentUser.getFollowing().contains(targetId)) {
            currentUser.getFollowing().remove(targetId);
            targetUser.getFollowers().remove(currentId);
        } else {
            currentUser.getFollowing().add(targetId);
            targetUser.getFollowers().add(currentId);
        }

        userRepository.save(currentUser);
        userRepository.save(targetUser);
    }

    public List<User> getSuggestedUsersToFollow(String currentUsername, int limit) {
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        Set<String> excludedIds = new HashSet<>(currentUser.getFollowing());
        excludedIds.add(currentUser.getId());

        return userRepository.findAll().stream()
                .filter(u -> !excludedIds.contains(u.getId()))
                .limit(limit)
                .toList();
    }

    public User updateProfile(String username, EditProfileRequest request) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setDisplayName(request.getDisplayName().trim());
        user.setBio(request.getBio() != null ? request.getBio().trim() : "");

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarPath = fileStorageService.saveFile(request.getAvatar());
            user.setAvatarUrl(avatarPath);
        }

        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            String bannerPath = fileStorageService.saveFile(request.getBanner());
            user.setBannerUrl(bannerPath);
        }

        return userRepository.save(user);
    }
}