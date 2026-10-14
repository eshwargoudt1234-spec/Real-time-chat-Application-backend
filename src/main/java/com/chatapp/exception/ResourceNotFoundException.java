package com.chatapp.exception;
/** Thrown when something (e.g. a user) does not exist -> HTTP 404 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
