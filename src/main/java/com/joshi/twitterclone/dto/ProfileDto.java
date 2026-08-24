package com.joshi.twitterclone.dto;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
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
public class ProfileDto {
    private User user;
    private boolean isSelf;
    private boolean isFollowing;
    private int followersCount;
    private int followingCount;
    private int tweetsCount;
    @Builder.Default
    private List<Tweet> tweets = new ArrayList<>();

    // Follower Count Aliases (singular vs plural)
    public int getFollowerCount() {
        return this.followersCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followersCount = followerCount;
    }

    public int getFollowersCount() {
        return this.followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    // Following Count Aliases
    public int getFollowingCount() {
        return this.followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getFollowingsCount() {
        return this.followingCount;
    }

    public void setFollowingsCount(int followingsCount) {
        this.followingCount = followingsCount;
    }

    // Tweet Count Aliases (singular vs plural)
    public int getTweetCount() {
        return this.tweetsCount;
    }

    public void setTweetCount(int tweetCount) {
        this.tweetsCount = tweetCount;
    }

    public int getTweetsCount() {
        return this.tweetsCount;
    }

    public void setTweetsCount(int tweetsCount) {
        this.tweetsCount = tweetsCount;
    }
}