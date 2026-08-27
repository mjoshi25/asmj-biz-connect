package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ConversationSummaryDto;
import com.joshi.twitterclone.dto.CreateGroupRequest;
import com.joshi.twitterclone.dto.DirectMessageDto;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.DirectMessageService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String viewMessages(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(value = "convo", required = false) String convoId,
                               Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<ConversationSummaryDto> conversations = directMessageService.getUserConversations(username);
        List<User> availableUsers = directMessageService.getAvailableUsersForChat(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("conversations", conversations);
        model.addAttribute("availableUsers", availableUsers);
        model.addAttribute("activeConvoId", convoId);

        if (convoId != null && !convoId.isBlank()) {
            Conversation activeConvo = directMessageService.getConversationById(convoId, username);
            List<DirectMessageDto> messages = directMessageService.getConversationMessages(convoId, username);

            model.addAttribute("activeConversation", activeConvo);
            model.addAttribute("messages", messages);
        } else {
            model.addAttribute("activeConversation", null);
            model.addAttribute("messages", Collections.emptyList());
        }

        return "messages";
    }

    @PostMapping("/direct/start")
    public String startDirectChat(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam("recipient") String recipient) {
        Conversation convo = directMessageService.getOrCreateDirectConversation(userDetails.getUsername(), recipient);
        return "redirect:/messages?convo=" + convo.getId();
    }

    @PostMapping("/group/create")
    public String createGroupChat(@AuthenticationPrincipal UserDetails userDetails,
                                  @ModelAttribute CreateGroupRequest request) {
        Conversation group = directMessageService.createGroupConversation(
                userDetails.getUsername(),
                request.getGroupName(),
                request.getMemberUsernames()
        );
        return "redirect:/messages?convo=" + group.getId();
    }

    @PostMapping("/group/{id}/add-members")
    public String addMembers(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable("id") String conversationId,
                             @RequestParam("memberUsernames") List<String> memberUsernames) {
        directMessageService.addMembersToGroup(userDetails.getUsername(), conversationId, memberUsernames);
        return "redirect:/messages?convo=" + conversationId;
    }

    @PostMapping("/group/{id}/exit")
    public String exitGroup(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable("id") String conversationId) {
        directMessageService.exitGroup(userDetails.getUsername(), conversationId);
        return "redirect:/messages";
    }

    @PostMapping("/send")
    public String sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("conversationId") String conversationId,
                              @RequestParam(value = "content", required = false) String content,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                              Model model) {
        String username = userDetails.getUsername();
        var savedMsg = directMessageService.sendMessage(username, conversationId, content, file);

        try {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, savedMsg);
        } catch (Exception ignored) {
        }

        if ("true".equalsIgnoreCase(hxRequest)) {
            List<DirectMessageDto> messages = directMessageService.getConversationMessages(conversationId, username);
            Conversation activeConvo = directMessageService.getConversationById(conversationId, username);

            model.addAttribute("activeConversation", activeConvo);
            model.addAttribute("messages", messages);
            return "fragments/message-panel :: message-bubble-list";
        }

        return "redirect:/messages?convo=" + conversationId;
    }

    @GetMapping("/feed/{conversationId}")
    public String pollMessages(@PathVariable("conversationId") String conversationId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        String username = userDetails.getUsername();
        List<DirectMessageDto> messages = directMessageService.getConversationMessages(conversationId, username);
        Conversation activeConvo = directMessageService.getConversationById(conversationId, username);

        model.addAttribute("activeConversation", activeConvo);
        model.addAttribute("messages", messages);
        return "fragments/message-panel :: message-bubble-list";
    }
}