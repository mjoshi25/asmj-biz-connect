package com.joshi.twitterclone.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.joshi.twitterclone.model.DirectMessage;

public interface DirectMessageRepository extends MongoRepository<DirectMessage, String> {

    // Fetch messages for an active conversation ordered by time
    List<DirectMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    // Mark messages in a conversation as read
    @Query("{ 'conversationId': ?0, 'isRead': false }")
    @Update("{ '$set': { 'isRead': true } }")
    void markConversationAsRead(String conversationId);
}