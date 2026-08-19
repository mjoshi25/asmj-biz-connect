package com.joshi.twitterclone.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.util.TweetTextProcessor;
import com.joshi.twitterclone.websocket.FeedWebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TweetBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final FeedWebSocketHandler webSocketHandler;
    private final SpringTemplateEngine templateEngine;
    private final TweetTextProcessor tweetTextProcessor;

    public void broadcastNewTweet(Tweet tweet) {
        Context context = new Context();
        context.setVariable("tweet", tweet);
        context.setVariable("currentUserId", "");
        // Provide the processor variable directly to avoid SpEL BeanResolver lookup
        context.setVariable("tweetTextProcessor", tweetTextProcessor);

        String renderedTweetCard = templateEngine.process(
                "fragments/tweet-components",
                Collections.singleton("tweet-card"),
                context
        );

        // 1. STOMP payload
        Map<String, Object> stompPayload = new HashMap<>();
        stompPayload.put("tweetId", tweet.getId());
        stompPayload.put("authorUsername", tweet.getAuthorUsername());
        stompPayload.put("html", renderedTweetCard);
        messagingTemplate.convertAndSend("/topic/tweets", stompPayload);

        // 2. Native HTMX ws-connect OOB swap
        String htmxOob = String.format(
                "<div id=\"tweet-feed-list\" hx-swap-oob=\"afterbegin\">%s</div>",
                renderedTweetCard
        );
        webSocketHandler.broadcastHtml(htmxOob);
    }

    public void broadcastMediaReady(String tweetId, String imageUrl, String blurDataUrl) {
        String mediaReplacementHtml = String.format(
                """
                <div id="tweet-media-%s" hx-swap-oob="outerHTML">
                    <div class="mt-3 overflow-hidden rounded-2xl border border-gray-800 relative aspect-video bg-gray-900">
                        <div class="absolute inset-0 bg-cover bg-center filter blur-md scale-105"
                             style="background-image: url('%s');"></div>
                        <img src="%s" 
                             class="relative w-full h-full object-cover opacity-0 transition-opacity duration-300"
                             onload="this.classList.remove('opacity-0');" />
                    </div>
                </div>
                """,
                tweetId, blurDataUrl, imageUrl
        );
        webSocketHandler.broadcastHtml(mediaReplacementHtml);
    }
}