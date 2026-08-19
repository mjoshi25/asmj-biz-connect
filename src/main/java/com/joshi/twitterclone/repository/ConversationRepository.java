package com.joshi.twitterclone.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.joshi.twitterclone.model.Conversation;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByParticipantIdsContainingOrderByLastMessageTimeDesc(String userId);
}