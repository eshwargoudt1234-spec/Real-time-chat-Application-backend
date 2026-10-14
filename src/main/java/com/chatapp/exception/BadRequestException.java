package com.chatapp.exception;
/** Thrown for invalid input (empty message, passwords not matching...) -> HTTP 400 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
