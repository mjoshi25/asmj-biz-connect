package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ThreadDto;
import com.joshi.twitterclone.dto.TrendDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public Tweet createTweetWithAsyncImage(String username, String content, MultipartFile imageFile) {
        User author = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet tweet = new Tweet();
        tweet.setAuthorId(author.getId());
        tweet.setAuthorUsername(author.getUsername());
        tweet.setAuthorDisplayName(author.getDisplayName());
        tweet.setContent(content != null ? content.trim() : "");
        tweet.setCreatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.saveImageOptimized(imageFile);
            tweet.setImageUrl(imageUrl);
        }

        return tweetRepository.save(tweet);
    }

    public Tweet createReply(String username, String parentTweetId, String content) {
        User author = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tweet parent = tweetRepository.findById(parentTweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent post not found"));

        Tweet reply = new Tweet();
        reply.setAuthorId(author.getId());
        reply.setAuthorUsername(author.getUsername());
        reply.setAuthorDisplayName(author.getDisplayName());
        reply.setContent(content != null ? content.trim() : "");
        reply.setParentTweetId(parentTweetId);
        reply.setCreatedAt(LocalDateTime.now());

        parent.setRepliesCount(parent.getRepliesCount() + 1);
        tweetRepository.save(parent);

        return tweetRepository.save(reply);
    }

    public Page<Tweet> getTimelineSlice(String username, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return tweetRepository.findAllByOrderByCreatedAtDesc(pageRequest);
    }

    public ThreadDto getThread(String tweetId) {
        Tweet root = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        List<Tweet> replies = tweetRepository.findByParentTweetIdOrderByCreatedAtAsc(tweetId);
        
        return ThreadDto.builder()
                .rootTweet(root)
                .mainTweet(root)
                .replies(replies)
                .build();
    }

    public Page<Tweet> getTweetsByHashtag(String hashtag, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String pattern = "#" + hashtag.replace("#", "");
        return tweetRepository.findByContentRegex(pattern, pageRequest);
    }

    public Page<Tweet> searchTweets(String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return tweetRepository.findByContentRegex(query, pageRequest);
    }

    public Tweet toggleLike(String tweetId, String username) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Set<String> likedBy = tweet.getLikedByUsernames();
        if (likedBy == null) {
            likedBy = new HashSet<>();
            tweet.setLikedByUsernames(likedBy);
        }

        if (likedBy.contains(username.toLowerCase())) {
            likedBy.remove(username.toLowerCase());
            tweet.setLikesCount(Math.max(0, tweet.getLikesCount() - 1));
        } else {
            likedBy.add(username.toLowerCase());
            tweet.setLikesCount(tweet.getLikesCount() + 1);
        }

        return tweetRepository.save(tweet);
    }

    public void deleteTweet(String tweetId, String requestingUsername) {
        deleteTweet(tweetId, requestingUsername, false);
    }

    public void deleteTweet(String tweetId, String requestingUsername, boolean isAdmin) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!isAdmin && !tweet.getAuthorUsername().equalsIgnoreCase(requestingUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this post.");
        }

        if (tweet.getImageUrl() != null && !tweet.getImageUrl().isBlank()) {
            fileStorageService.deleteByUrl(tweet.getImageUrl());
        }

        tweetRepository.delete(tweet);
        log.info("Tweet [{}] deleted by user [{}]", tweetId, requestingUsername);
    }

    public List<Tweet> getAllTweets() {
        return tweetRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Returns structured trending topics matching sidebar template requirements.
     */
    public List<TrendDto> getTrendingHashtags() {
        return List.of(
                TrendDto.builder().hashtag("ASMJBizConnect").category("Business · Trending").tweetCount(1420).build(),
                TrendDto.builder().hashtag("SpringBoot3").category("Technology · Trending").tweetCount(890).build(),
                TrendDto.builder().hashtag("AdMarketplace").category("Commerce · Trending").tweetCount(530).build(),
                TrendDto.builder().hashtag("Cloudinary").category("Cloud Storage · Trending").tweetCount(310).build(),
                TrendDto.builder().hashtag("Innovation").category("Global · Trending").tweetCount(240).build()
        );
    }
}