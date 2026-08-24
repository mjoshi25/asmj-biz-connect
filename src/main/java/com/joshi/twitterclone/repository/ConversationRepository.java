package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query("{ 'isGroup': false, 'participantUsernames': { $all: [?0, ?1], $size: 2 } }")
    Optional<Conversation> findDirectConversation(String user1, String user2);

    List<Conversation> findByParticipantUsernamesContainingOrderByUpdatedAtDesc(String username);
}