package com.chatapp.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** JSON shape returned for every REST error. */
public record ErrorResponse(int status, String error, String message,
                            LocalDateTime timestamp, Map<String, String> fieldErrors) { }
