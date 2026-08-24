package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ConversationSummaryDto;
import com.joshi.twitterclone.dto.CreateGroupRequest;
import com.joshi.twitterclone.dto.DirectMessageDto;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.service.DirectMessageService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
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

    private final DirectMessageService messageService;
    private final UserService userService;

    @GetMapping
    public String viewMessages(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(value = "convo", required = false) String activeConvoId,
                               Model model) {
        String currentUsername = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(currentUsername);
        List<ConversationSummaryDto> conversations = messageService.getUserConversations(currentUsername);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("conversations", conversations);

        if (activeConvoId != null && !activeConvoId.isBlank()) {
            try {
                List<DirectMessageDto> messages = messageService.getConversationMessages(activeConvoId, currentUsername);
                Conversation activeConversation = messageService.getConversationById(activeConvoId);

                model.addAttribute("activeConvoId", activeConvoId);
                model.addAttribute("activeConversation", activeConversation);
                model.addAttribute("messages", messages);
            } catch (Exception e) {
                // If conversation does not exist or user lacks access, fall back gracefully to main chat list
                model.addAttribute("activeConvoId", null);
                model.addAttribute("messages", Collections.emptyList());
            }
        } else if (!conversations.isEmpty()) {
            String firstConvoId = conversations.get(0).getConversationId();
            return "redirect:/messages?convo=" + firstConvoId;
        }

        return "messages";
    }

    @PostMapping("/direct/start")
    public String startDirectConversation(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam("recipient") String recipientUsername) {
        String currentUsername = userDetails.getUsername();
        Conversation convo = messageService.getOrCreateDirectConversation(currentUsername, recipientUsername);
        return "redirect:/messages?convo=" + convo.getId();
    }

    @GetMapping("/conversation/{convoId}")
    public String getConversationFragment(@PathVariable("convoId") String convoId,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          Model model) {
        String currentUsername = userDetails.getUsername();
        List<DirectMessageDto> messages = messageService.getConversationMessages(convoId, currentUsername);
        Conversation activeConversation = messageService.getConversationById(convoId);

        model.addAttribute("currentUser", userService.getUserByUsername(currentUsername));
        model.addAttribute("activeConvoId", convoId);
        model.addAttribute("activeConversation", activeConversation);
        model.addAttribute("messages", messages);

        return "fragments/message-panel :: chat-window";
    }

    @PostMapping("/send")
    public String sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("conversationId") String conversationId,
                              @RequestParam(value = "content", required = false) String content,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              Model model) {
        String currentUsername = userDetails.getUsername();
        messageService.sendMessage(currentUsername, conversationId, content, file);

        List<DirectMessageDto> messages = messageService.getConversationMessages(conversationId, currentUsername);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", userService.getUserByUsername(currentUsername));

        return "fragments/message-panel :: message-bubble-list";
    }

    @PostMapping("/group/create")
    public String createGroup(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute CreateGroupRequest request) {
        Conversation group = messageService.createGroupConversation(userDetails.getUsername(), request);
        return "redirect:/messages?convo=" + group.getId();
    }

    @PostMapping("/group/{convoId}/members/add")
    public String addMembers(@PathVariable("convoId") String convoId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("usernames") List<String> usernames) {
        messageService.addMembersToGroup(convoId, userDetails.getUsername(), usernames);
        return "redirect:/messages?convo=" + convoId;
    }

    @PostMapping("/group/{convoId}/leave")
    public String leaveGroup(@PathVariable("convoId") String convoId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        messageService.leaveGroup(convoId, userDetails.getUsername());
        return "redirect:/messages";
    }
}