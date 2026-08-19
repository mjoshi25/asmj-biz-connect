package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.AutocompleteResultDto;
import com.joshi.twitterclone.dto.AutocompleteResultDto.HashtagSuggestionDto;
import com.joshi.twitterclone.dto.AutocompleteResultDto.UserSuggestionDto;
import com.joshi.twitterclone.repository.TweetSearchRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final TweetSearchRepository tweetSearchRepository;

    public AutocompleteResultDto getAutocompleteSuggestions(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return AutocompleteResultDto.builder()
                    .query("")
                    .users(Collections.emptyList())
                    .hashtags(Collections.emptyList())
                    .build();
        }

        String query = rawQuery.trim();
        List<UserSuggestionDto> userResults = Collections.emptyList();
        List<HashtagSuggestionDto> tagResults = Collections.emptyList();

        if (query.startsWith("#")) {
            tagResults = tweetSearchRepository.findHashtagsByPrefix(query.substring(1), 5);
        } else if (query.startsWith("@")) {
            userResults = userRepository.findTop5ByPrefix(query.substring(1))
                    .stream()
                    .map(u -> new UserSuggestionDto(u.getUsername(), u.getDisplayName()))
                    .toList();
        } else {
            userResults = userRepository.findTop5ByPrefix(query)
                    .stream()
                    .map(u -> new UserSuggestionDto(u.getUsername(), u.getDisplayName()))
                    .toList();
            tagResults = tweetSearchRepository.findHashtagsByPrefix(query, 4);
        }

        return AutocompleteResultDto.builder()
                .query(query)
                .users(userResults)
                .hashtags(tagResults)
                .build();
    }
}