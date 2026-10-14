package com.chatapp.exception;
/** Thrown for duplicate username/email -> HTTP 409 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}
