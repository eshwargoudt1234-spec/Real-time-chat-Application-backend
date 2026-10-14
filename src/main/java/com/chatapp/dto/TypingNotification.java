package com.chatapp.dto;
/** Pushed to the receiver on /user/queue/typing */
public record TypingNotification(Long senderId, String senderName, boolean typing) { }
