package com.joshi.twitterclone.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.dto.ConversationSummaryDto;
import com.joshi.twitterclone.dto.CreateGroupRequest;
import com.joshi.twitterclone.dto.DirectMessageDto;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.DirectMessage;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.DirectMessageRepository;
import com.joshi.twitterclone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a · MMM d");

    public Conversation createGroupConversation(String creatorUsername, CreateGroupRequest request) {
        User creator = userRepository.findByUsername(creatorUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Set<String> participantIds = new HashSet<>();
        Set<String> participantUsernames = new HashSet<>();

        participantIds.add(creator.getId());
        participantUsernames.add(creator.getUsername());

        if (request.getMemberUsernames() != null) {
            for (String memberUsername : request.getMemberUsernames()) {
                if (memberUsername == null || memberUsername.isBlank()) continue;
                userRepository.findByUsername(memberUsername.toLowerCase().trim()).ifPresent(user -> {
                    participantIds.add(user.getId());
                    participantUsernames.add(user.getUsername());
                });
            }
        }

        if (participantIds.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one other member to create a group.");
        }

        Conversation group = new Conversation();
        group.setId("group_" + UUID.randomUUID().toString().replace("-", ""));
        group.setGroup(true);
        group.setName(request.getName().trim());
        group.setCreatedByUserId(creator.getId());
        group.setParticipantIds(participantIds);
        group.setParticipantUsernames(participantUsernames);
        group.setLastMessageContent("Group created");
        group.setLastSenderUsername(creator.getUsername());
        group.setLastSenderDisplayName(creator.getDisplayName());
        group.setLastMessageTime(LocalDateTime.now());

        return conversationRepository.save(group);
    }

    public Conversation getOrCreateDirectConversation(String user1Name, String user2Name) {
        User u1 = userRepository.findByUsername(user1Name.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User u2 = userRepository.findByUsername(user2Name.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String conversationId = Conversation.buildDirectConversationId(u1.getId(), u2.getId());

        return conversationRepository.findById(conversationId).orElseGet(() -> {
            Conversation c = new Conversation();
            c.setId(conversationId);
            c.setGroup(false);
            c.setParticipantIds(Set.of(u1.getId(), u2.getId()));
            c.setParticipantUsernames(Set.of(u1.getUsername(), u2.getUsername()));
            return conversationRepository.save(c);
        });
    }

    public DirectMessage sendMessage(String senderUsername, String conversationId, String content, MultipartFile file) {
        boolean hasText = content != null && !content.trim().isEmpty();
        boolean hasFile = file != null && !file.isEmpty();

        if (!hasText && !hasFile) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message text or attachment is required.");
        }

        User sender = userRepository.findByUsername(senderUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.getParticipantIds().contains(sender.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are no longer a participant in this conversation.");
        }

        // 1. Create message entity
        DirectMessage msg = new DirectMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(sender.getId());
        msg.setSenderUsername(sender.getUsername());
        msg.setSenderDisplayName(sender.getDisplayName());
        msg.setSenderAvatarUrl(sender.getAvatarUrl());
        msg.setContent(hasText ? content.trim() : "");

        // 2. Persist media attachment if present
        if (hasFile) {
            String savedFilePath = fileStorageService.saveFile(file);
            msg.setMediaUrl(savedFilePath);
            msg.setOriginalFileName(file.getOriginalFilename());

            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                msg.setMediaType("IMAGE");
            } else {
                msg.setMediaType("FILE");
            }
        }

        DirectMessage savedMessage = messageRepository.save(msg);

        // 3. Update Conversation metadata snippet & unread counters
        String snippet = hasText
                ? savedMessage.getContent()
                : ("IMAGE".equals(savedMessage.getMediaType()) ? "📷 Photo" : "📎 File: " + savedMessage.getOriginalFileName());

        convo.setLastMessageContent(snippet);
        convo.setLastSenderUsername(sender.getUsername());
        convo.setLastSenderDisplayName(sender.getDisplayName());
        convo.setLastMessageTime(LocalDateTime.now());

        for (String participantId : convo.getParticipantIds()) {
            if (!participantId.equals(sender.getId())) {
                int count = convo.getUnreadCounts().getOrDefault(participantId, 0);
                convo.getUnreadCounts().put(participantId, count + 1);
            }
        }
        conversationRepository.save(convo);

        // 4. Dispatch WebSocket DTO to all conversation participants
        String conversationTitle = convo.isGroup() ? convo.getName() : sender.getDisplayName();

        DirectMessageDto pushDto = DirectMessageDto.builder()
                .id(savedMessage.getId())
                .conversationId(conversationId)
                .conversationTitle(conversationTitle)
                .isGroup(convo.isGroup())
                .senderUsername(sender.getUsername())
                .senderDisplayName(sender.getDisplayName())
                .senderAvatarUrl(sender.getAvatarUrl())
                .content(savedMessage.getContent())
                .mediaUrl(savedMessage.getMediaUrl())
                .mediaType(savedMessage.getMediaType())
                .originalFileName(savedMessage.getOriginalFileName())
                .createdAtFormatted(savedMessage.getCreatedAt().format(TIME_FORMATTER))
                .isSelf(false)
                .build();

        for (String participantUsername : convo.getParticipantUsernames()) {
            if (!participantUsername.equalsIgnoreCase(sender.getUsername())) {
                messagingTemplate.convertAndSendToUser(
                        participantUsername,
                        "/queue/messages",
                        pushDto
                );
            }
        }

        return savedMessage;
    }

    public List<ConversationSummaryDto> getUserConversations(String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Conversation> conversations = conversationRepository
                .findByParticipantIdsContainingOrderByLastMessageTimeDesc(currentUser.getId());

        List<ConversationSummaryDto> summaries = new ArrayList<>();
        for (Conversation c : conversations) {
            if (c.isGroup()) {
                summaries.add(ConversationSummaryDto.builder()
                        .conversationId(c.getId())
                        .isGroup(true)
                        .title(c.getName())
                        .avatarUrl(c.getGroupAvatarUrl())
                        .defaultInitial(c.getName() != null && !c.getName().isEmpty() ? c.getName().substring(0, 1).toUpperCase() : "G")
                        .memberUsernames(new ArrayList<>(c.getParticipantUsernames()))
                        .lastMessage(c.getLastMessageContent())
                        .lastSenderName(c.getLastSenderDisplayName())
                        .lastMessageTimeFormatted(c.getLastMessageTime().format(DateTimeFormatter.ofPattern("MMM d")))
                        .unreadCount(c.getUnreadCounts().getOrDefault(currentUser.getId(), 0))
                        .build());
            } else {
                String otherUserId = c.getParticipantIds().stream()
                        .filter(id -> !id.equals(currentUser.getId()))
                        .findFirst()
                        .orElse(currentUser.getId());

                User otherUser = userRepository.findById(otherUserId).orElse(null);
                if (otherUser != null) {
                    summaries.add(ConversationSummaryDto.builder()
                            .conversationId(c.getId())
                            .isGroup(false)
                            .title(otherUser.getDisplayName())
                            .avatarUrl(otherUser.getAvatarUrl())
                            .defaultInitial(otherUser.getUsername() != null && !otherUser.getUsername().isEmpty() ? otherUser.getUsername().substring(0, 1).toUpperCase() : "U")
                            .otherUser(otherUser)
                            .lastMessage(c.getLastMessageContent())
                            .lastSenderName(c.getLastSenderDisplayName())
                            .lastMessageTimeFormatted(c.getLastMessageTime().format(DateTimeFormatter.ofPattern("MMM d")))
                            .unreadCount(c.getUnreadCounts().getOrDefault(currentUser.getId(), 0))
                            .build());
                }
            }
        }
        return summaries;
    }

    public List<DirectMessageDto> getConversationMessages(String currentUsername, String conversationId) {
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.getParticipantIds().contains(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        messageRepository.markConversationAsRead(conversationId);
        convo.getUnreadCounts().put(currentUser.getId(), 0);
        conversationRepository.save(convo);

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> DirectMessageDto.builder()
                        .id(m.getId())
                        .conversationId(conversationId)
                        .conversationTitle(convo.isGroup() ? convo.getName() : m.getSenderDisplayName())
                        .isGroup(convo.isGroup())
                        .senderUsername(m.getSenderUsername())
                        .senderDisplayName(m.getSenderDisplayName())
                        .senderAvatarUrl(m.getSenderAvatarUrl())
                        .content(m.getContent())
                        .mediaUrl(m.getMediaUrl())
                        .mediaType(m.getMediaType())
                        .originalFileName(m.getOriginalFileName())
                        .createdAtFormatted(m.getCreatedAt().format(TIME_FORMATTER))
                        .isSelf(m.getSenderId().equals(currentUser.getId()))
                        .build())
                .toList();
    }

    public Conversation addMembersToGroup(String currentUsername, String conversationId, List<String> newMemberUsernames) {
        if (newMemberUsernames == null || newMemberUsernames.isEmpty()) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        }

        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.isGroup() || !convo.getParticipantIds().contains(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to manage this group.");
        }

        List<String> addedDisplayNames = new ArrayList<>();
        for (String username : newMemberUsernames) {
            if (username == null || username.isBlank()) continue;
            userRepository.findByUsername(username.toLowerCase().trim()).ifPresent(u -> {
                if (!convo.getParticipantIds().contains(u.getId())) {
                    convo.getParticipantIds().add(u.getId());
                    convo.getParticipantUsernames().add(u.getUsername());
                    addedDisplayNames.add("@" + u.getUsername());
                }
            });
        }

        if (!addedDisplayNames.isEmpty()) {
            conversationRepository.save(convo);
            sendMessage(currentUsername, conversationId, "📢 " + currentUser.getDisplayName() + " added " + String.join(", ", addedDisplayNames) + " to the group.", null);
        }

        return convo;
    }

    public void leaveGroup(String currentUsername, String conversationId) {
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.isGroup() || !convo.getParticipantIds().contains(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot exit this conversation.");
        }

        convo.getParticipantIds().remove(currentUser.getId());
        convo.getParticipantUsernames().remove(currentUser.getUsername());
        convo.getUnreadCounts().remove(currentUser.getId());

        if (convo.getParticipantIds().isEmpty()) {
            conversationRepository.delete(convo);
        } else {
            conversationRepository.save(convo);
            sendMessage(currentUsername, conversationId, "👋 " + currentUser.getDisplayName() + " (@" + currentUser.getUsername() + ") left the group.", null);
        }
    }

    public List<User> getNonMembersForGroup(String conversationId, String currentUsername) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        return userRepository.findAll().stream()
                .filter(u -> !convo.getParticipantIds().contains(u.getId()))
                .toList();
    }

    public List<User> getGroupMembers(String conversationId, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.getParticipantIds().contains(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view members.");
        }

        return (List<User>) userRepository.findAllById(convo.getParticipantIds());
    }
}