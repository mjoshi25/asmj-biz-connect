package com.joshi.twitterclone.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.util.TweetTextProcessor;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final TweetTextProcessor tweetTextProcessor;
    private final UserRepository userRepository;

    @ModelAttribute("tweetTextProcessor")
    public TweetTextProcessor tweetTextProcessor() {
        return tweetTextProcessor;
    }

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername().toLowerCase()).orElse(null);
    }
}