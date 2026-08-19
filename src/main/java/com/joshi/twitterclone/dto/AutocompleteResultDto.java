package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutocompleteResultDto {
    private String query;
    private List<UserSuggestionDto> users;
    private List<HashtagSuggestionDto> hashtags;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSuggestionDto {
        private String username;
        private String displayName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HashtagSuggestionDto {
        private String tag;
        private long count;
    }
}