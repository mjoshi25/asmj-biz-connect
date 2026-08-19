package com.joshi.twitterclone.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.dto.AddMembersRequest;
import com.joshi.twitterclone.dto.ConversationSummaryDto;
import com.joshi.twitterclone.dto.CreateGroupRequest;
import com.joshi.twitterclone.dto.DirectMessageDto;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.DirectMessage;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.ConversationRepository;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.service.DirectMessageService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final TweetService tweetService;

    @GetMapping("/messages")
    public String viewMessages(@RequestParam(name = "convo", required = false) String convoId,
                               @RequestParam(name = "user", required = false) String directTargetUsername,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        String currentUsername = userDetails.getUsername();
        List<ConversationSummaryDto> conversations = directMessageService.getUserConversations(currentUsername);

        model.addAttribute("conversations", conversations);
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", userService.getSuggestedUsersToFollow(currentUsername, 4));

        String activeConversationId = convoId;

        if (activeConversationId == null && directTargetUsername != null && !directTargetUsername.isBlank()) {
            Conversation directConvo = directMessageService.getOrCreateDirectConversation(currentUsername, directTargetUsername);
            activeConversationId = directConvo.getId();
        }

        if (activeConversationId != null) {
            Conversation convo = conversationRepository.findById(activeConversationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
            List<DirectMessageDto> messages = directMessageService.getConversationMessages(currentUsername, activeConversationId);

            String chatTitle = convo.getName();
            if (!convo.isGroup()) {
                String otherUser = convo.getParticipantUsernames().stream()
                        .filter(u -> !u.equalsIgnoreCase(currentUsername))
                        .findFirst().orElse(currentUsername);
                chatTitle = "@" + otherUser;
            }

            model.addAttribute("activeConversation", convo);
            model.addAttribute("chatTitle", chatTitle);
            model.addAttribute("messages", messages);
        }

        return "messages";
    }

    @GetMapping("/messages/thread/{convoId}")
    public String getConversationThread(@PathVariable("convoId") String convoId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        String currentUsername = userDetails.getUsername();
        Conversation convo = conversationRepository.findById(convoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        List<DirectMessageDto> messages = directMessageService.getConversationMessages(currentUsername, convoId);

        String chatTitle = convo.getName();
        if (!convo.isGroup()) {
            String otherUser = convo.getParticipantUsernames().stream()
                    .filter(u -> !u.equalsIgnoreCase(currentUsername))
                    .findFirst().orElse(currentUsername);
            chatTitle = "@" + otherUser;
        }

        model.addAttribute("activeConversation", convo);
        model.addAttribute("chatTitle", chatTitle);
        model.addAttribute("messages", messages);

        return "fragments/message-components :: chat-thread";
    }

    @GetMapping("/messages/modal/new")
    public String getUnifiedNewChatModal(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String currentUsername = userDetails.getUsername();
        List<User> availableUsers = userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equalsIgnoreCase(currentUsername))
                .toList();

        model.addAttribute("availableUsers", availableUsers);
        return "fragments/chat-modals :: new-chat-modal";
    }

    @GetMapping("/messages/modal/{convoId}/add-members")
    public String getAddMembersModal(@PathVariable("convoId") String convoId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        List<User> nonMembers = directMessageService.getNonMembersForGroup(convoId, userDetails.getUsername());
        model.addAttribute("convoId", convoId);
        model.addAttribute("availableUsers", nonMembers);
        return "fragments/chat-modals :: add-members-modal";
    }

    @GetMapping("/messages/modal/{convoId}/members")
    public String getGroupMembersModal(@PathVariable("convoId") String convoId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        List<User> members = directMessageService.getGroupMembers(convoId, userDetails.getUsername());
        model.addAttribute("convoId", convoId);
        model.addAttribute("members", members);
        return "fragments/chat-modals :: view-members-modal";
    }

    @PostMapping("/messages/direct/start")
    public String startDirectChat(@RequestParam("recipientUsername") String recipientUsername,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Conversation direct = directMessageService.getOrCreateDirectConversation(userDetails.getUsername(), recipientUsername);
        return "redirect:/messages?convo=" + direct.getId();
    }

    @PostMapping("/messages/group/create")
    public String createGroup(@ModelAttribute CreateGroupRequest request,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Conversation group = directMessageService.createGroupConversation(userDetails.getUsername(), request);
        return "redirect:/messages?convo=" + group.getId();
    }

    @PostMapping("/messages/group/{convoId}/add")
    public String addMembersToGroup(@PathVariable("convoId") String convoId,
                                    @ModelAttribute AddMembersRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        directMessageService.addMembersToGroup(userDetails.getUsername(), convoId, request.getMemberUsernames());
        return "redirect:/messages?convo=" + convoId;
    }

    @PostMapping("/messages/group/{convoId}/leave")
    public String leaveGroup(@PathVariable("convoId") String convoId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        directMessageService.leaveGroup(userDetails.getUsername(), convoId);
        return "redirect:/messages";
    }

    @PostMapping("/messages/send")
    public String sendMessage(@RequestParam("conversationId") String conversationId,
                              @RequestParam(value = "content", required = false) String content,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        String currentUsername = userDetails.getUsername();
        DirectMessage msg = directMessageService.sendMessage(currentUsername, conversationId, content, file);

        DirectMessageDto dto = DirectMessageDto.builder()
                .id(msg.getId())
                .conversationId(conversationId)
                .isGroup(conversationId.startsWith("group_"))
                .senderUsername(msg.getSenderUsername())
                .senderDisplayName(msg.getSenderDisplayName())
                .senderAvatarUrl(msg.getSenderAvatarUrl())
                .content(msg.getContent())
                .mediaUrl(msg.getMediaUrl())
                .mediaType(msg.getMediaType())
                .originalFileName(msg.getOriginalFileName())
                .createdAtFormatted(msg.getCreatedAt().format(DateTimeFormatter.ofPattern("h:mm a · MMM d")))
                .isSelf(true)
                .build();

        model.addAttribute("msg", dto);
        return "fragments/message-components :: message-bubble";
    }
}