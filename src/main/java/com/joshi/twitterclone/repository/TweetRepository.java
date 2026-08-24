package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String> {

    List<Tweet> findAllByOrderByCreatedAtDesc();

    Page<Tweet> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Required by UserService
    List<Tweet> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    List<Tweet> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);

    @Query("{ 'parentTweetId': ?0 }")
    List<Tweet> findByParentTweetIdOrderByCreatedAtAsc(String parentTweetId);

    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    Page<Tweet> findByContentRegex(String regexPattern, Pageable pageable);

    Page<Tweet> findByAuthorUsernameInOrderByCreatedAtDesc(List<String> usernames, Pageable pageable);
}