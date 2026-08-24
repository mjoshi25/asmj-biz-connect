package com.joshi.twitterclone.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mm a · MMM d");

    public Conversation getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found: " + conversationId));
    }

    public Conversation getOrCreateDirectConversation(String user1, String user2) {
        String u1 = user1.toLowerCase().trim();
        String u2 = user2.toLowerCase().trim();

        // Check if both users exist
        userRepository.findByUsername(u2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient @" + user2 + " not found"));

        return conversationRepository.findDirectConversation(u1, u2)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setGroup(false);
                    c.setParticipantUsernames(new HashSet<>(Set.of(u1, u2)));
                    c.setCreatedAt(LocalDateTime.now());
                    c.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(c);
                });
    }

    public List<ConversationSummaryDto> getUserConversations(String currentUsername) {
        String username = currentUsername.toLowerCase().trim();
        List<Conversation> convos = conversationRepository.findByParticipantUsernamesContainingOrderByUpdatedAtDesc(username);

        return convos.stream().map(c -> {
            String title = c.getName();
            String initial = "G";

            if (!c.isGroup()) {
                String otherUser = c.getParticipantUsernames().stream()
                        .filter(u -> !u.equalsIgnoreCase(username))
                        .findFirst().orElse("User");

                User u = userRepository.findByUsername(otherUser).orElse(null);
                title = (u != null && u.getDisplayName() != null && !u.getDisplayName().isBlank()) 
                        ? u.getDisplayName() 
                        : otherUser;
                initial = !title.isEmpty() ? title.substring(0, 1).toUpperCase() : "U";
            }

            return ConversationSummaryDto.builder()
                    .conversationId(c.getId())
                    .title(title != null ? title : "Chat")
                    .lastMessage(c.getLastMessage() != null ? c.getLastMessage() : "No messages yet")
                    .lastSenderName(c.getLastSenderName())
                    .lastMessageTimeFormatted(c.getUpdatedAt() != null ? c.getUpdatedAt().format(FORMATTER) : "")
                    .defaultInitial(initial)
                    .isGroup(c.isGroup())
                    .unreadCount(0)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<DirectMessageDto> getConversationMessages(String conversationId, String currentUsername) {
        Conversation convo = getConversationById(conversationId);

        if (!convo.getParticipantUsernames().contains(currentUsername.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view messages");
        }

        List<DirectMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(m -> mapToDto(m, m.getSenderUsername().equalsIgnoreCase(currentUsername), convo.isGroup(), convo.getName()))
                .collect(Collectors.toList());
    }

    public DirectMessage sendMessage(String senderUsername, String conversationId, String content, MultipartFile file) {
        User sender = userRepository.findByUsername(senderUsername.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        Conversation convo = getConversationById(conversationId);

        if (!convo.getParticipantUsernames().contains(senderUsername.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not in conversation");
        }

        DirectMessage msg = new DirectMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(sender.getId());
        msg.setSenderUsername(sender.getUsername());
        msg.setSenderDisplayName(sender.getDisplayName());
        msg.setSenderAvatarUrl(sender.getAvatarUrl());
        msg.setContent(content != null ? content.trim() : "");
        msg.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            String uploadedUrl = fileStorageService.saveFile(file);
            msg.setMediaUrl(uploadedUrl);
            msg.setOriginalFileName(file.getOriginalFilename());
            String ct = file.getContentType();
            msg.setMediaType((ct != null && ct.startsWith("image/")) ? "IMAGE" : "FILE");
        }

        DirectMessage savedMsg = messageRepository.save(msg);

        String snippet = (msg.getContent() != null && !msg.getContent().isBlank())
                ? msg.getContent()
                : (msg.getMediaType() != null ? "Attachment" : "Message");

        convo.setLastMessage(snippet);
        convo.setLastSenderName(sender.getDisplayName());
        convo.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(convo);

        for (String participant : convo.getParticipantUsernames()) {
            boolean isSelf = participant.equalsIgnoreCase(senderUsername);
            DirectMessageDto dto = mapToDto(savedMsg, isSelf, convo.isGroup(), convo.getName());
            messagingTemplate.convertAndSendToUser(participant, "/queue/messages", dto);
        }

        return savedMsg;
    }

    public Conversation createGroupConversation(String creatorUsername, CreateGroupRequest request) {
        Conversation group = new Conversation();
        group.setGroup(true);
        group.setName(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "Group Chat");

        Set<String> participants = new HashSet<>();
        participants.add(creatorUsername.toLowerCase().trim());
        if (request.getMemberUsernames() != null) {
            request.getMemberUsernames().forEach(u -> participants.add(u.toLowerCase().trim()));
        }

        group.setParticipantUsernames(participants);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(group);
    }

    public void addMembersToGroup(String conversationId, String requestingUsername, List<String> newUsernames) {
        Conversation convo = getConversationById(conversationId);

        if (!convo.isGroup() || !convo.getParticipantUsernames().contains(requestingUsername.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        if (newUsernames != null) {
            newUsernames.forEach(u -> convo.getParticipantUsernames().add(u.toLowerCase().trim()));
        }
        convo.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(convo);
    }

    public void leaveGroup(String conversationId, String username) {
        Conversation convo = getConversationById(conversationId);

        convo.getParticipantUsernames().remove(username.toLowerCase().trim());
        if (convo.getParticipantUsernames().isEmpty()) {
            conversationRepository.delete(convo);
        } else {
            convo.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(convo);
        }
    }

    public List<User> getGroupMembers(String conversationId, String currentUsername) {
        Conversation convo = getConversationById(conversationId);

        return userRepository.findAll().stream()
                .filter(u -> convo.getParticipantUsernames().contains(u.getUsername().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<User> getNonMembersForGroup(String conversationId, String currentUsername) {
        Conversation convo = getConversationById(conversationId);

        return userRepository.findAll().stream()
                .filter(u -> !convo.getParticipantUsernames().contains(u.getUsername().toLowerCase()))
                .collect(Collectors.toList());
    }

    private DirectMessageDto mapToDto(DirectMessage msg, boolean isSelf, boolean isGroup, String convoTitle) {
        return DirectMessageDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderUsername(msg.getSenderUsername())
                .senderDisplayName(msg.getSenderDisplayName())
                .senderAvatarUrl(msg.getSenderAvatarUrl())
                .content(msg.getContent())
                .mediaUrl(msg.getMediaUrl())
                .mediaType(msg.getMediaType())
                .originalFileName(msg.getOriginalFileName())
                .createdAtFormatted(msg.getCreatedAt() != null ? msg.getCreatedAt().format(FORMATTER) : "")
                .isSelf(isSelf)
                .isGroup(isGroup)
                .conversationTitle(convoTitle)
                .build();
    }
}