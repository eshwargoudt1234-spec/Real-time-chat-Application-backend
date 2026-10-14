package com.chatapp.dto;

import com.chatapp.entity.Message;
import com.chatapp.entity.MessageStatus;
import java.time.LocalDateTime;

public record MessageDto(Long id, Long senderId, Long receiverId, String content,
                         LocalDateTime timestamp, MessageStatus status) {
    public static MessageDto from(Message m) {
        return new MessageDto(m.getId(), m.getSender().getId(), m.getReceiver().getId(),
                m.getContent(), m.getTimestamp(), m.getStatus());
    }
}
