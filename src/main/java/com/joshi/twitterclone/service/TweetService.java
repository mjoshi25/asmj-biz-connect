package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ThreadDto;
import com.joshi.twitterclone.dto.TrendDto;
import com.joshi.twitterclone.model.MediaStatus;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public List<Tweet> getRecentTweets() {
        return tweetRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Tweet createTweet(String username, String content, MultipartFile image) {
        User user = userService.getUserByUsername(username);

        Tweet tweet = new Tweet();
        tweet.setAuthorId(user.getId());
        tweet.setAuthorUsername(user.getUsername());
        tweet.setAuthorDisplayName(user.getDisplayName());
        tweet.setContent(content);
        tweet.setLikesCount(0);
        tweet.setRepliesCount(0);
        tweet.setLikedByUsernames(new HashSet<>());
        tweet.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            String mediaUrl = fileStorageService.saveImageOptimized(image);
            tweet.setMediaUrl(mediaUrl);
            tweet.setMediaStatus(MediaStatus.READY);
        } else {
            tweet.setMediaStatus(MediaStatus.NONE);
        }

        return tweetRepository.save(tweet);
    }

    public Tweet replyToTweet(String username, String parentTweetId, String content, MultipartFile image) {
        Tweet parent = tweetRepository.findById(parentTweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent post not found"));

        User user = userService.getUserByUsername(username);

        Tweet reply = new Tweet();
        reply.setAuthorId(user.getId());
        reply.setAuthorUsername(user.getUsername());
        reply.setAuthorDisplayName(user.getDisplayName());
        reply.setParentTweetId(parentTweetId);
        reply.setContent(content);
        reply.setLikesCount(0);
        reply.setRepliesCount(0);
        reply.setLikedByUsernames(new HashSet<>());
        reply.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            String mediaUrl = fileStorageService.saveImageOptimized(image);
            reply.setMediaUrl(mediaUrl);
            reply.setMediaStatus(MediaStatus.READY);
        } else {
            reply.setMediaStatus(MediaStatus.NONE);
        }

        Tweet savedReply = tweetRepository.save(reply);

        parent.setRepliesCount(parent.getRepliesCount() + 1);
        tweetRepository.save(parent);

        return savedReply;
    }

    public ThreadDto getThread(String tweetId, String currentUsername) {
        Tweet mainTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        List<Tweet> replies = tweetRepository.findByParentTweetIdOrderByCreatedAtAsc(tweetId);

        return ThreadDto.builder()
                .mainTweet(mainTweet)
                .replies(replies != null ? replies : Collections.emptyList())
                .build();
    }

    public ThreadDto getThread(String tweetId) {
        return getThread(tweetId, null);
    }

    public Tweet toggleLike(String tweetId, String username) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

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

    public List<TrendDto> getTrendingHashtags() {
        Map<String, Integer> counts = new HashMap<>();
        Pattern pattern = Pattern.compile("#(\\w+)");

        List<Tweet> recent = getRecentTweets();
        for (Tweet t : recent) {
            if (t.getContent() != null) {
                Matcher matcher = pattern.matcher(t.getContent());
                while (matcher.find()) {
                    String tag = matcher.group(1).toLowerCase();
                    counts.put(tag, counts.getOrDefault(tag, 0) + 1);
                }
            }
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> new TrendDto(e.getKey(), e.getValue(), "Trending"))
                .collect(Collectors.toList());
    }
}