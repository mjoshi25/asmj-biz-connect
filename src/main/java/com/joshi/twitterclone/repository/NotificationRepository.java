package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Slice<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);
    long countByRecipientIdAndIsReadFalse(String recipientId);

    @Query("{ 'recipientId': ?0, 'isRead': false }")
    @Update("{ '$set': { 'isRead': true } }")
    void markAllAsReadForUser(String recipientId);
}