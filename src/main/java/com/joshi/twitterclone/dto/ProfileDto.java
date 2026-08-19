package com.joshi.twitterclone.dto;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileDto {
    private User user;
    private List<Tweet> tweets;
    private int followerCount;
    private int followingCount;
    private int tweetCount;
    private boolean isSelf;
    private boolean isFollowing;
}