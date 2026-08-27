package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ConversationSummaryDto;
import com.joshi.twitterclone.dto.DirectMessageDto;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.DirectMessage;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.DirectMessageRepository;
import com.joshi.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public List<ConversationSummaryDto> getUserConversations(String username) {
        if (username == null || username.isBlank()) {
            return Collections.emptyList();
        }

        List<Conversation> conversations = conversationRepository.findByParticipantUsernamesContainingOrderByUpdatedAtDesc(username.toLowerCase().trim());
        
        return conversations.stream().map((Conversation c) -> {
            String title;
            String defaultInitial;
            
            if (c.isGroup()) {
                title = c.getName() != null && !c.getName().isBlank() ? c.getName() : "Group Chat";
                defaultInitial = title.substring(0, 1).toUpperCase();
            } else {
                String otherUsername = c.getParticipantUsernames().stream()
                        .filter(u -> !u.equalsIgnoreCase(username))
                        .findFirst()
                        .orElse(username);
                
                User otherUser = userRepository.findByUsername(otherUsername.toLowerCase()).orElse(null);
                title = otherUser != null ? otherUser.getDisplayName() : "@" + otherUsername;
                defaultInitial = title.substring(0, 1).toUpperCase();
            }

            String formattedTime = "";
            if (c.getUpdatedAt() != null) {
                formattedTime = c.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM d"));
            }

            int count = c.getParticipantUsernames() != null ? c.getParticipantUsernames().size() : 0;

            return ConversationSummaryDto.builder()
                    .conversationId(c.getId())
                    .title(title)
                    .defaultInitial(defaultInitial)
                    .lastMessage(c.getLastMessage() != null ? c.getLastMessage() : "No messages yet")
                    .lastMessageTimeFormatted(formattedTime)
                    .isGroup(c.isGroup())
                    .participantCount(count)
                    .build();
        }).collect(Collectors.toList());
    }

    public Conversation getConversationById(String conversationId, String currentUsername) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (currentUsername != null && !convo.getParticipantUsernames().contains(currentUsername.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }

        return convo;
    }

    public Conversation getConversationById(String conversationId) {
        return getConversationById(conversationId, null);
    }

    public Conversation getOrCreateDirectConversation(String user1, String user2) {
        String u1 = user1.toLowerCase().trim();
        String u2 = user2.toLowerCase().trim();

        List<Conversation> existing = conversationRepository.findByParticipantUsernamesContainingOrderByUpdatedAtDesc(u1);
        for (Conversation c : existing) {
            if (!c.isGroup() && c.getParticipantUsernames().contains(u2) && c.getParticipantUsernames().size() == 2) {
                return c;
            }
        }

        Conversation newConvo = new Conversation();
        newConvo.setGroup(false);
        newConvo.setParticipantUsernames(new HashSet<>(Set.of(u1, u2)));
        newConvo.setCreatedAt(LocalDateTime.now());
        newConvo.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(newConvo);
    }

    public Conversation createGroupConversation(String creatorUsername, String groupName, List<String> memberUsernames) {
        if (groupName == null || groupName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be blank");
        }

        Set<String> participants = new HashSet<>();
        participants.add(creatorUsername.toLowerCase().trim());

        if (memberUsernames != null) {
            for (String member : memberUsernames) {
                if (member != null && !member.isBlank()) {
                    participants.add(member.toLowerCase().trim());
                }
            }
        }

        if (participants.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A group must have at least 2 participants");
        }

        Conversation group = new Conversation();
        group.setGroup(true);
        group.setName(groupName.trim());
        group.setParticipantUsernames(participants);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        group.setLastMessage("Group created by @" + creatorUsername);
        group.setLastSenderName(creatorUsername);

        return conversationRepository.save(group);
    }

    public void addMembersToGroup(String username, String conversationId, List<String> newMembers) {
        Conversation convo = getConversationById(conversationId, username);
        if (!convo.isGroup()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add members to a direct message");
        }

        if (newMembers != null) {
            for (String member : newMembers) {
                if (member != null && !member.isBlank()) {
                    convo.getParticipantUsernames().add(member.toLowerCase().trim());
                }
            }
        }

        convo.setUpdatedAt(LocalDateTime.now());
        convo.setLastMessage("@" + username + " added new members");
        conversationRepository.save(convo);
    }

    public void exitGroup(String username, String conversationId) {
        Conversation convo = getConversationById(conversationId, username);
        if (!convo.isGroup()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot exit a direct message");
        }

        convo.getParticipantUsernames().remove(username.toLowerCase().trim());

        if (convo.getParticipantUsernames().isEmpty()) {
            conversationRepository.delete(convo);
            return;
        }

        convo.setUpdatedAt(LocalDateTime.now());
        convo.setLastMessage("@" + username + " left the group");
        conversationRepository.save(convo);
    }

    public List<DirectMessageDto> getConversationMessages(String conversationId, String currentUsername) {
        List<DirectMessage> messages = directMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        return messages.stream().map(m -> {
            boolean isSelf = m.getSenderUsername().equalsIgnoreCase(currentUsername);
            String formattedTime = m.getCreatedAt() != null 
                    ? m.getCreatedAt().format(DateTimeFormatter.ofPattern("hh:mm a")) 
                    : "";

            return DirectMessageDto.builder()
                    .id(m.getId())
                    .conversationId(m.getConversationId())
                    .senderUsername(m.getSenderUsername())
                    .senderDisplayName(m.getSenderDisplayName())
                    .content(m.getContent())
                    .mediaUrl(m.getMediaUrl())
                    .mediaType(m.getMediaType())
                    .originalFileName(m.getOriginalFileName())
                    .isSelf(isSelf)
                    .createdAtFormatted(formattedTime)
                    .build();
        }).collect(Collectors.toList());
    }

    public DirectMessage sendMessage(String senderUsername, String conversationId, String content, MultipartFile file) {
        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!convo.getParticipantUsernames().contains(senderUsername.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        User sender = userRepository.findByUsername(senderUsername.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        DirectMessage msg = new DirectMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(sender.getId());
        msg.setSenderUsername(sender.getUsername());
        msg.setSenderDisplayName(sender.getDisplayName());
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            String url;
            if (contentType != null && contentType.startsWith("image/")) {
                url = fileStorageService.saveImageOptimized(file);
                msg.setMediaType("IMAGE");
            } else {
                url = fileStorageService.saveFile(file);
                msg.setMediaType("FILE");
            }
            msg.setMediaUrl(url);
            msg.setOriginalFileName(file.getOriginalFilename());
        }

        DirectMessage savedMsg = directMessageRepository.save(msg);

        convo.setLastMessage(content != null && !content.isBlank() ? content : "[Attachment]");
        convo.setLastSenderName(sender.getDisplayName());
        convo.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(convo);

        return savedMsg;
    }

    public List<User> getAvailableUsersForChat(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equalsIgnoreCase(currentUsername))
                .sorted(Comparator.comparing(User::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}