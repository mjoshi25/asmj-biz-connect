package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingHashtagDto {
    private String hashtag;
    private long tweetCount;
}