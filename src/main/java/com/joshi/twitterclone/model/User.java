package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String displayName;
    private String email;
    private String password;
    private String bio;
    private String avatarUrl;
    private String bannerUrl;

    // Roles: ROLE_USER, ROLE_ADMIN
    private Set<String> roles = new HashSet<>();

    private Set<String> following = new HashSet<>();
    private Set<String> followers = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}