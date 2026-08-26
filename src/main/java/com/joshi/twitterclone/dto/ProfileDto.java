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
    private int tweetsCount;
    @Builder.Default
    private List<Tweet> tweets = new ArrayList<>();

    public int getTweetCount() {
        return this.tweetsCount;
    }

    public void setTweetCount(int tweetCount) {
        this.tweetsCount = tweetCount;
    }
}