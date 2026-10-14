package com.chatapp.controller;

import com.chatapp.dto.ConversationDto;
import com.chatapp.dto.MessageDto;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Private endpoints. "Me" is always taken from the JWT, so you can only ever see YOUR conversations. */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final ChatService chatService;

    public MessageController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** GET /api/messages/conversations -> recent conversations (last message + unread count) */
    @GetMapping("/conversations")
    public List<ConversationDto> conversations(Authentication auth) {
        return chatService.getConversations(auth.getName());
    }

    /** GET /api/messages/{userId} -> chat history between me and {userId} */
    @GetMapping("/{userId}")
    public List<MessageDto> conversation(@PathVariable Long userId, Authentication auth) {
        return chatService.getConversation(auth.getName(), userId);
    }

    /** PUT /api/messages/{userId}/read -> mark their messages to me as READ */
    @PutMapping("/{userId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long userId, Authentication auth) {
        chatService.markAsRead(auth.getName(), userId);
        return ResponseEntity.noContent().build();
    }
}
