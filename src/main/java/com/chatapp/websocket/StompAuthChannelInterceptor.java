package com.chatapp.websocket;

import com.chatapp.security.CustomUserDetailsService;
import com.chatapp.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Security for WebSocket: the browser must send "Authorization: Bearer <jwt>" in the STOMP CONNECT frame.
 * If it is valid, the username becomes the session's Principal (used by convertAndSendToUser and @MessageMapping).
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new MessagingException("Unauthorized: missing token");
            }
            String token = header.substring(7);
            if (!jwtUtil.isValid(token)) {
                throw new MessagingException("Unauthorized: invalid or expired token");
            }
            UserDetails user;
            try {
                user = userDetailsService.loadUserByUsername(jwtUtil.extractUsername(token));
            } catch (UsernameNotFoundException e) {
                throw new MessagingException("Unauthorized: user no longer exists");
            }
            accessor.setUser(new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities()));

        } else if (accessor.getUser() == null &&
                (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
            throw new MessagingException("Unauthorized: please connect with a valid token first");
        }
        return message;
    }
}
