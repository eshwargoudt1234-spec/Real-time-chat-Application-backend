package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/auth/login (usernameOrEmail accepts either) */
public record LoginRequest(
        @NotBlank(message = "Username or email is required") String usernameOrEmail,
        @NotBlank(message = "Password is required") String password) { }
