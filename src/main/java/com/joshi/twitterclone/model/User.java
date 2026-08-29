package com.joshi.twitterclone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    private String email;
    private String displayName;
    private String bio;
    private String location;
    private String website;
    private String avatarUrl;
    private String bannerUrl;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private List<String> following = new ArrayList<>();

    @Builder.Default
    private List<String> followers = new ArrayList<>();

    @Builder.Default
    private boolean privateAccount = false;

    @Builder.Default
    private boolean showEmailToPublic = false;

    @Builder.Default
    private boolean allowDirectMessagesFromEveryone = true;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getJoinedDateFormatted() {
        if (createdAt != null) {
            return createdAt.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        }
        return "Joined recently";
    }
}