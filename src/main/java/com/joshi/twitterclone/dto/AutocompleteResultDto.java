package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutocompleteResultDto {

    @Builder.Default
    private List<String> suggestions = new ArrayList<>();

    @Builder.Default
    private List<HashtagSuggestionDto> hashtags = new ArrayList<>();

    @Builder.Default
    private List<UserSuggestionDto> users = new ArrayList<>();

    @Builder.Default
    private List<String> usernames = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HashtagSuggestionDto {
        private String tag;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSuggestionDto {
        private String username;
        private String displayName;
        private String avatarUrl;
    }
}