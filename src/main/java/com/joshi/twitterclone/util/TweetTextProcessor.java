package com.joshi.twitterclone.util;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("tweetTextProcessor")
public class TweetTextProcessor {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([a-zA-Z0-9_]+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{3,20})");

    public static Set<String> extractHashtags(String content) {
        Set<String> tags = new HashSet<>();
        if (content == null || content.isBlank()) {
            return tags;
        }
        Matcher matcher = HASHTAG_PATTERN.matcher(content);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }

    public static Set<String> extractMentions(String content) {
        Set<String> mentions = new HashSet<>();
        if (content == null || content.isBlank()) {
            return mentions;
        }
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1).toLowerCase());
        }
        return mentions;
    }

    public String formatContentWithLinks(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String withMentions = MENTION_PATTERN.matcher(content)
                .replaceAll("<a href=\"/u/$1\" class=\"text-sky-400 hover:underline\">@$1</a>");
        return HASHTAG_PATTERN.matcher(withMentions)
                .replaceAll("<a href=\"/hashtag/$1\" class=\"text-sky-400 hover:underline\">#$1</a>");
    }
}