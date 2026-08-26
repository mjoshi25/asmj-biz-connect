package com.joshi.twitterclone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Indexed(unique = true)
    private String email;

    private String password;
    private String avatarUrl;
    private String bannerUrl;
    private String bio;
    private String location;
    private String website;

    private Set<String> roles = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getJoinedDateFormatted() {
        if (createdAt == null) return "Joined Recently";
        return "Joined " + createdAt.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }
}