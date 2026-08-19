package com.joshi.twitterclone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TweetMediaUpdateDto {
    private String tweetId;
    private String imageUrl;
    private String blurHash;
    private String blurDataUrl;
    private String status;
}