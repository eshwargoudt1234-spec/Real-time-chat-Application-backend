package com.chatapp.websocket;

import com.chatapp.dto.ChatMessageRequest;
import com.chatapp.dto.TypingRequest;
import com.chatapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Receives STOMP frames sent by the browser to /app/...
 *   /app/chat   -> send a message
 *   /app/typing -> typing indicator on/off
 */
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        chatService.sendMessage(principal.getName(), request);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        chatService.notifyTyping(principal.getName(), request);
    }

    /** Any error in the methods above is sent back ONLY to the user who caused it: /user/queue/errors */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, String> handleException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Something went wrong";
        return Map.of("message", msg);
    }
}
