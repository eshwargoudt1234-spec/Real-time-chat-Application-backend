package com.chatapp.dto;
/**
 * Pushed to a sender on /user/queue/receipts.
 * type = "DELIVERED" or "READ"; userId = the person who received/read the messages.
 */
public record ReceiptEvent(String type, Long userId) { }
