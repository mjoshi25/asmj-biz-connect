package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ThreadViewDto;
import com.joshi.twitterclone.dto.TrendingTopicDto;
import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public List<Tweet> getAllTweets() {
        return tweetRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Tweet> getTweetsByAuthors(Set<String> authorUsernames) {
        if (authorUsernames == null || authorUsernames.isEmpty()) {
            return Collections.emptyList();
        }
        return tweetRepository.findByAuthorUsernameInOrderByCreatedAtDesc(authorUsernames);
    }

    public Tweet getTweetById(String tweetId) {
        return tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found: " + tweetId));
    }

    public ThreadViewDto getThread(String tweetId) {
        Tweet mainTweet = getTweetById(tweetId);
        List<Tweet> replies = tweetRepository.findByParentTweetIdOrderByCreatedAtAsc(tweetId);
        return ThreadViewDto.builder()
                .mainTweet(mainTweet)
                .replies(replies != null ? replies : new ArrayList<>())
                .build();
    }

    public Tweet createTweet(String username, String content, MultipartFile image) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
        }

        Tweet tweet = new Tweet();
        tweet.setAuthorId(user.getId());
        tweet.setAuthorUsername(user.getUsername());
        tweet.setAuthorDisplayName(user.getDisplayName());
        tweet.setAuthorAvatarUrl(user.getAvatarUrl());
        tweet.setContent(content != null ? content.trim() : "");
        tweet.setCreatedAt(LocalDateTime.now());
        tweet.setLikesCount(0);
        tweet.setRepliesCount(0);
        tweet.setLikedByUsernames(new HashSet<>());
        tweet.setMediaStatus(MediaStatus.NONE);

        if (image != null && !image.isEmpty()) {
            String mediaUrl = fileStorageService.saveImageOptimized(image);
            tweet.setImageUrl(mediaUrl);
            tweet.setMediaStatus(MediaStatus.READY);
        }

        return tweetRepository.save(tweet);
    }

    public Tweet createReply(String username, String parentTweetId, String content) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
        }

        Tweet parentTweet = getTweetById(parentTweetId);

        Tweet reply = new Tweet();
        reply.setAuthorId(user.getId());
        reply.setAuthorUsername(user.getUsername());
        reply.setAuthorDisplayName(user.getDisplayName());
        reply.setAuthorAvatarUrl(user.getAvatarUrl());
        reply.setParentTweetId(parentTweetId);
        reply.setContent(content != null ? content.trim() : "");
        reply.setCreatedAt(LocalDateTime.now());
        reply.setLikesCount(0);
        reply.setRepliesCount(0);
        reply.setLikedByUsernames(new HashSet<>());
        reply.setMediaStatus(MediaStatus.NONE);

        Tweet savedReply = tweetRepository.save(reply);

        parentTweet.setRepliesCount(parentTweet.getRepliesCount() + 1);
        tweetRepository.save(parentTweet);

        return savedReply;
    }

    public Tweet toggleLike(String tweetId, String username) {
        Tweet tweet = getTweetById(tweetId);
        if (tweet.getLikedByUsernames() == null) {
            tweet.setLikedByUsernames(new HashSet<>());
        }

        if (tweet.getLikedByUsernames().contains(username)) {
            tweet.getLikedByUsernames().remove(username);
            tweet.setLikesCount(Math.max(0, tweet.getLikesCount() - 1));
        } else {
            tweet.getLikedByUsernames().add(username);
            tweet.setLikesCount(tweet.getLikesCount() + 1);
        }

        return tweetRepository.save(tweet);
    }

    public List<Tweet> searchTweets(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        return tweetRepository.searchByContentRegex(query.trim(), pageRequest).getContent();
    }

    public List<Tweet> getTweetsByHashtag(String hashtag, int page, int size) {
        if (hashtag == null || hashtag.isBlank()) {
            return Collections.emptyList();
        }
        String cleanTag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
        return searchTweets(cleanTag, page, size);
    }

    public List<TrendingTopicDto> getTrendingHashtags() {
        return List.of(
                new TrendingTopicDto("BUSINESS", "ASMJBizConnect", 1420),
                new TrendingTopicDto("TECHNOLOGY", "SpringBoot3", 890),
                new TrendingTopicDto("COMMERCE", "AdMarketplace", 530),
                new TrendingTopicDto("CLOUD STORAGE", "Cloudinary", 310),
                new TrendingTopicDto("GLOBAL", "Innovation", 240)
        );
    }
}