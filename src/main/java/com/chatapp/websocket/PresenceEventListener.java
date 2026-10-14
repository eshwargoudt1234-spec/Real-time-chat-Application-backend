package com.chatapp.websocket;

import com.chatapp.dto.StatusEvent;
import com.chatapp.dto.UserDto;
import com.chatapp.entity.UserStatus;
import com.chatapp.service.ChatService;
import com.chatapp.service.PresenceService;
import com.chatapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/** Reacts to WebSocket connect/disconnect to update ONLINE/OFFLINE and broadcast it on /topic/status. */
@Component
public class PresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);

    private final PresenceService presenceService;
    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceEventListener(PresenceService presenceService, UserService userService,
                                 ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.userService = userService;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;
        try {
            String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
            if (presenceService.userConnected(sessionId, principal.getName())) {
                broadcast(userService.updateStatus(principal.getName(), UserStatus.ONLINE));
            }
            chatService.markAllDelivered(principal.getName());
        } catch (Exception e) {
            log.error("Error handling connect event", e);
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        try {
            String username = presenceService.userDisconnected(event.getSessionId());
            if (username != null) {
                broadcast(userService.updateStatus(username, UserStatus.OFFLINE));
            }
        } catch (Exception e) {
            log.error("Error handling disconnect event", e);
        }
    }

    private void broadcast(UserDto user) {
        messagingTemplate.convertAndSend("/topic/status",
                new StatusEvent(user.id(), user.username(), user.status()));
    }
}
