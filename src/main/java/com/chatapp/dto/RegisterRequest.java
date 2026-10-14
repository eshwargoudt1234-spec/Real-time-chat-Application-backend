package com.chatapp.dto;

import jakarta.validation.constraints.*;

/** Body of POST /api/auth/register */
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String name,

        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[a-zA-Z0-9_.]{3,30}$",
                 message = "Username must be 3-30 characters (letters, numbers, dot, underscore)")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please enter a valid email address")
        @Size(max = 120, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6-72 characters")
        String password,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword) { }
