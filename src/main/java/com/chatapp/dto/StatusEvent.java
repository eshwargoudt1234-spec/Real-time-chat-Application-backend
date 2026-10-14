package com.chatapp.dto;
import com.chatapp.entity.UserStatus;
/** Broadcast on /topic/status when someone goes online/offline */
public record StatusEvent(Long userId, String username, UserStatus status) { }
