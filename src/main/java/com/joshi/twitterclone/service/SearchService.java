package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.AutocompleteResultDto;
import com.joshi.twitterclone.dto.AutocompleteResultDto.HashtagSuggestionDto;
import com.joshi.twitterclone.dto.AutocompleteResultDto.UserSuggestionDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public List<Tweet> searchTweets(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String cleanQuery = query.toLowerCase().trim();
        return tweetRepository.findAll().stream()
                .filter(t -> (t.getContent() != null && t.getContent().toLowerCase().contains(cleanQuery))
                        || (t.getAuthorUsername() != null && t.getAuthorUsername().toLowerCase().contains(cleanQuery))
                        || (t.getAuthorDisplayName() != null && t.getAuthorDisplayName().toLowerCase().contains(cleanQuery)))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public AutocompleteResultDto getAutocompleteSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return new AutocompleteResultDto();
        }
        String cleanQuery = query.toLowerCase().trim();
        List<UserSuggestionDto> users = new ArrayList<>();
        List<String> usernames = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        Map<String, Integer> hashtagCounts = new HashMap<>();

        // Match Users
        userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(cleanQuery)
                        || (u.getDisplayName() != null && u.getDisplayName().toLowerCase().contains(cleanQuery)))
                .limit(4)
                .forEach(u -> {
                    usernames.add("@" + u.getUsername());
                    users.add(UserSuggestionDto.builder()
                            .username(u.getUsername())
                            .displayName(u.getDisplayName())
                            .avatarUrl(u.getAvatarUrl())
                            .build());
                });

        // Match Hashtags & Content Preview
        Pattern pattern = Pattern.compile("#(\\w+)");
        tweetRepository.findAll().stream()
                .filter(t -> t.getContent() != null && t.getContent().toLowerCase().contains(cleanQuery))
                .limit(10)
                .forEach(t -> {
                    Matcher matcher = pattern.matcher(t.getContent());
                    while (matcher.find()) {
                        String tag = matcher.group(1);
                        if (tag.toLowerCase().contains(cleanQuery)) {
                            hashtagCounts.put(tag, hashtagCounts.getOrDefault(tag, 0) + 1);
                        }
                    }
                    String preview = t.getContent().length() > 45 ? t.getContent().substring(0, 45) + "..." : t.getContent();
                    if (!suggestions.contains(preview)) {
                        suggestions.add(preview);
                    }
                });

        List<HashtagSuggestionDto> hashtags = hashtagCounts.entrySet().stream()
                .map(e -> HashtagSuggestionDto.builder()
                        .tag(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return AutocompleteResultDto.builder()
                .users(users)
                .usernames(usernames)
                .hashtags(hashtags)
                .suggestions(suggestions)
                .build();
    }
}