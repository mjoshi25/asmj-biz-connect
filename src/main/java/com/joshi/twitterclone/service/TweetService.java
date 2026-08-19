package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ThreadDto;
import com.joshi.twitterclone.dto.TrendingHashtagDto;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.TweetRepository;
import com.joshi.twitterclone.repository.TweetSearchRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.util.TweetTextProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final TweetSearchRepository tweetSearchRepository;
    private final AsyncImageProcessor asyncImageProcessor;
    private final TweetBroadcastService tweetBroadcastService;
    private final NotificationService notificationService;

    public Tweet createTweetWithAsyncImage(String username, String content, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tweet tweet = new Tweet();
        tweet.setAuthorId(user.getId());
        tweet.setAuthorUsername(user.getUsername());
        tweet.setAuthorDisplayName(user.getDisplayName());
        tweet.setContent(content != null ? content.trim() : "");
        tweet.setHashtags(TweetTextProcessor.extractHashtags(content));
        tweet.setMediaStatus(file != null && !file.isEmpty() ? Tweet.MediaStatus.PROCESSING : Tweet.MediaStatus.NONE);

        Tweet savedTweet = tweetRepository.save(tweet);

        if (file != null && !file.isEmpty()) {
            asyncImageProcessor.processTweetImageAsync(savedTweet.getId(), file.getBytes());
        }

        notificationService.processMentionNotifications(savedTweet);
        tweetBroadcastService.broadcastNewTweet(savedTweet);

        return savedTweet;
    }

    public Slice<Tweet> getTimelineSlice(String username, int page, int size) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> authorIds = new HashSet<>(user.getFollowing());
        authorIds.add(user.getId());

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return tweetRepository.findByAuthorIdInAndParentTweetIdIsNull(authorIds, pageRequest);
    }

    public Tweet toggleLike(String tweetId, String username) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found"));

        if (tweet.getLikedBy().contains(user.getId())) {
            tweet.getLikedBy().remove(user.getId());
        } else {
            tweet.getLikedBy().add(user.getId());
        }

        return tweetRepository.save(tweet);
    }

    public void deleteTweet(String tweetId, String username) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found"));

        if (!tweet.getAuthorId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized deletion");
        }

        tweetRepository.delete(tweet);
    }

    public Tweet createReply(String parentTweetId, String username, String content) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Tweet parent = tweetRepository.findById(parentTweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent tweet not found"));

        Tweet reply = new Tweet();
        reply.setAuthorId(user.getId());
        reply.setAuthorUsername(user.getUsername());
        reply.setAuthorDisplayName(user.getDisplayName());
        reply.setContent(content.trim());
        reply.setParentTweetId(parent.getId());
        reply.setHashtags(TweetTextProcessor.extractHashtags(content));

        Tweet savedReply = tweetRepository.save(reply);

        parent.setReplyCount(parent.getReplyCount() + 1);
        tweetRepository.save(parent);

        notificationService.processMentionNotifications(savedReply);
        return savedReply;
    }

    public ThreadDto getThread(String tweetId) {
        Tweet focalTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet not found"));

        Tweet parentTweet = null;
        if (focalTweet.getParentTweetId() != null) {
            parentTweet = tweetRepository.findById(focalTweet.getParentTweetId()).orElse(null);
        }

        List<Tweet> replies = tweetRepository.findByParentTweetIdOrderByCreatedAtAsc(focalTweet.getId());

        return ThreadDto.builder()
                .focalTweet(focalTweet)
                .parentTweet(parentTweet)
                .replies(replies)
                .build();
    }

    public Slice<Tweet> getTweetsByHashtag(String hashtag, int page, int size) {
        String cleanTag = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        return tweetRepository.findByHashtagsContainingIgnoreCaseOrderByCreatedAtDesc(
                cleanTag.toLowerCase(), PageRequest.of(page, size)
        );
    }

    public Slice<Tweet> searchTweets(String searchTerm, int page, int size) {
        if (searchTerm.startsWith("#")) {
            return getTweetsByHashtag(searchTerm, page, size);
        }
        return tweetRepository.searchByText(searchTerm, PageRequest.of(page, size));
    }

    public List<TrendingHashtagDto> getTrendingHashtags() {
        return tweetSearchRepository.getTrendingHashtags(24, 5);
    }
}