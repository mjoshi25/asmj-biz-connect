package com.joshi.twitterclone.dto;

import com.joshi.twitterclone.model.Tweet;
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
public class ThreadViewDto {
    private Tweet mainTweet;
    @Builder.Default
    private List<Tweet> replies = new ArrayList<>();
}