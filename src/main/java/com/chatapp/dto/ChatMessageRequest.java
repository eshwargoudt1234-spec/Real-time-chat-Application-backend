package com.chatapp.dto;
/** Sent by the browser to /app/chat */
public record ChatMessageRequest(Long receiverId, String content) { }
