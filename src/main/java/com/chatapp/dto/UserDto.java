package com.chatapp.dto;

import com.chatapp.entity.User;
import com.chatapp.entity.UserStatus;
import java.time.LocalDateTime;

/** Safe version of User sent to the browser (NO password). */
public record UserDto(Long id, String name, String username, String email,
                      UserStatus status, LocalDateTime createdAt) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getUsername(), u.getEmail(), u.getStatus(), u.getCreatedAt());
    }
}
