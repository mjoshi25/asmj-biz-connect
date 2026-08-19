package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.dto.AutocompleteResultDto;
import com.joshi.twitterclone.dto.TrendingHashtagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TweetSearchRepository {

    private final MongoTemplate mongoTemplate;

    public List<TrendingHashtagDto> getTrendingHashtags(int hoursLookback, int limit) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursLookback);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(cutoff)
                        .and("hashtags").exists(true).ne(List.of())),
                Aggregation.unwind("hashtags"),
                Aggregation.group("hashtags").count().as("tweetCount"),
                Aggregation.project("tweetCount").and("_id").as("hashtag"),
                Aggregation.sort(Sort.Direction.DESC, "tweetCount"),
                Aggregation.limit(limit)
        );

        AggregationResults<TrendingHashtagDto> results = mongoTemplate.aggregate(
                aggregation, "tweets", TrendingHashtagDto.class
        );

        return results.getMappedResults();
    }

    public List<AutocompleteResultDto.HashtagSuggestionDto> findHashtagsByPrefix(String prefix, int limit) {
        String cleanPrefix = prefix.startsWith("#") ? prefix.substring(1) : prefix;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("hashtags"),
                Aggregation.match(Criteria.where("hashtags").regex("^" + cleanPrefix, "i")),
                Aggregation.group("hashtags").count().as("count"),
                Aggregation.project("count").and("_id").as("tag"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(limit)
        );

        AggregationResults<AutocompleteResultDto.HashtagSuggestionDto> results = mongoTemplate.aggregate(
                aggregation, "tweets", AutocompleteResultDto.HashtagSuggestionDto.class
        );

        return results.getMappedResults();
    }
}