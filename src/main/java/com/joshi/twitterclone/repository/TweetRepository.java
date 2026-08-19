package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Tweet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface TweetRepository extends MongoRepository<Tweet, String> {
    Slice<Tweet> findByAuthorIdInAndParentTweetIdIsNull(Collection<String> authorIds, Pageable pageable);
    List<Tweet> findByAuthorIdOrderByCreatedAtDesc(String authorId);
    List<Tweet> findByParentTweetIdOrderByCreatedAtAsc(String parentTweetId);
    Slice<Tweet> findByHashtagsContainingIgnoreCaseOrderByCreatedAtDesc(String hashtag, Pageable pageable);

    @Query(value = "{ $text: { $search: ?0 } }")
    Slice<Tweet> searchByText(String textQuery, Pageable pageable);
}