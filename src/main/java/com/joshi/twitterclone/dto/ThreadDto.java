package com.joshi.twitterclone.dto;

import com.joshi.twitterclone.model.Tweet;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ThreadDto {
    private Tweet parentTweet;
    private Tweet focalTweet;
    private List<Tweet> replies;
}