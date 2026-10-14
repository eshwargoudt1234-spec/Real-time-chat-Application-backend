package com.chatapp.dto;
/** Returned after a successful login: the JWT token + the user's profile */
public record AuthResponse(String token, UserDto user) { }
