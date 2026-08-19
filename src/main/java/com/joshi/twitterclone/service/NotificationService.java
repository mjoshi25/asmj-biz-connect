package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.NotificationPushDto;
import com.joshi.twitterclone.model.Notification;
import com.joshi.twitterclone.model.Tweet;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.NotificationRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.util.TweetTextProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    public void processMentionNotifications(Tweet tweet) {
        Set<String> mentionedUsernames = TweetTextProcessor.extractMentions(tweet.getContent());
        if (mentionedUsernames.isEmpty()) {
            return;
        }

        String snippet = tweet.getContent().length() > 80
                ? tweet.getContent().substring(0, 77) + "..."
                : tweet.getContent();

        for (String username : mentionedUsernames) {
            if (username.equalsIgnoreCase(tweet.getAuthorUsername())) {
                continue;
            }

            Optional<User> recipientOpt = userRepository.findByUsername(username.toLowerCase());
            if (recipientOpt.isPresent()) {
                User recipient = recipientOpt.get();

                Notification notif = new Notification();
                notif.setRecipientId(recipient.getId());
                notif.setActorId(tweet.getAuthorId());
                notif.setActorUsername(tweet.getAuthorUsername());
                notif.setActorDisplayName(tweet.getAuthorDisplayName());
                notif.setType(Notification.NotificationType.MENTION);
                notif.setTargetTweetId(tweet.getId());
                notif.setSnippet(snippet);

                Notification savedNotif = notificationRepository.save(notif);
                long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(recipient.getId());

                NotificationPushDto payload = NotificationPushDto.builder()
                        .id(savedNotif.getId())
                        .type(savedNotif.getType().name())
                        .actorUsername(savedNotif.getActorUsername())
                        .actorDisplayName(savedNotif.getActorDisplayName())
                        .targetTweetId(savedNotif.getTargetTweetId())
                        .snippet(savedNotif.getSnippet())
                        .unreadCount(unreadCount)
                        .createdAt(savedNotif.getCreatedAt().format(TIME_FORMATTER))
                        .build();

                messagingTemplate.convertAndSendToUser(
                        recipient.getUsername(),
                        "/queue/notifications",
                        payload
                );
            }
        }
    }

    public Slice<Notification> getUserNotifications(String userId, int page, int size) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}