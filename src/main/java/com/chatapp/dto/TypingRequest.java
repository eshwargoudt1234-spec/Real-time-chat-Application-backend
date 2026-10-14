package com.chatapp.dto;
/** Sent by the browser to /app/typing */
public record TypingRequest(Long receiverId, boolean typing) { }
