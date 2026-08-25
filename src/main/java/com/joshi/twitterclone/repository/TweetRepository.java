package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String> {
    List<Tweet> findByAuthorIdOrderByCreatedAtDesc(String authorId);
    List<Tweet> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);
    List<Tweet> findByAuthorUsernameInOrderByCreatedAtDesc(Collection<String> authorUsernames);
    List<Tweet> findByParentTweetIdOrderByCreatedAtAsc(String parentTweetId);

    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    Page<Tweet> searchByContentRegex(String queryRegex, Pageable pageable);
}