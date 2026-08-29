package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Notification;
import com.joshi.twitterclone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(String username, String title, String message, String targetUrl) {
        if (username == null || username.isBlank()) return;
        Notification n = Notification.builder()
                .recipientUsername(username.toLowerCase().trim())
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username.toLowerCase().trim());
    }

    public long getUnreadCount(String username) {
        if (username == null || username.isBlank()) return 0;
        return notificationRepository.countByRecipientUsernameAndReadFalse(username.toLowerCase().trim());
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}