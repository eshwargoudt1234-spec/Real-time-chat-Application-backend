package com.chatapp.dto;
/** One row in "Recent conversations" */
public record ConversationDto(UserDto user, MessageDto lastMessage, long unreadCount) { }
