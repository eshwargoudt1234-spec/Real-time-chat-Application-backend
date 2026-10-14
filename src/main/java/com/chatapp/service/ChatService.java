package com.chatapp.service;

import com.chatapp.dto.*;
import com.chatapp.entity.Message;
import com.chatapp.entity.MessageStatus;
import com.chatapp.entity.User;
import com.chatapp.exception.BadRequestException;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/** All chat business logic: save messages, push them in real time, track delivered/read. */
@Service
@Transactional
public class ChatService {

    private static final int MAX_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository, UserRepository userRepository,
                       PresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    // ------------------------------------------------------------------ sending

    /** Saves a message and pushes it to BOTH the receiver and the sender (so the sender gets the DB id/status). */
    public MessageDto sendMessage(String senderUsername, ChatMessageRequest req) {
        if (req == null || req.receiverId() == null) {
            throw new BadRequestException("Please choose who to send the message to");
        }
        String content = req.content() == null ? "" : req.content().trim();
        if (content.isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }
        if (content.length() > MAX_LENGTH) {
            throw new BadRequestException("Message is too long (max " + MAX_LENGTH + " characters)");
        }

        User sender = findByUsername(senderUsername);
        User receiver = userRepository.findById(req.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        // If the receiver is connected right now, the message counts as delivered immediately
        message.setStatus(presenceService.isOnline(receiver.getUsername())
                ? MessageStatus.DELIVERED : MessageStatus.SENT);
        message = messageRepository.save(message);

        MessageDto dto = MessageDto.from(message);
        afterCommit(() -> {
            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", dto);
            messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/messages", dto);
        });
        return dto;
    }

    // ------------------------------------------------------------------ typing

    public void notifyTyping(String senderUsername, TypingRequest req) {
        if (req == null || req.receiverId() == null) return;
        User sender = findByUsername(senderUsername);
        userRepository.findById(req.receiverId()).ifPresent(receiver ->
                messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/typing",
                        new TypingNotification(sender.getId(), sender.getName(), req.typing())));
    }

    // ------------------------------------------------------------------ status (delivered / read)

    /** Called when a user connects: everything waiting for them (SENT) becomes DELIVERED. */
    public void markAllDelivered(String username) {
        User me = findByUsername(username);
        List<Long> senderIds = messageRepository.findSenderIdsByReceiverAndStatus(me.getId(), MessageStatus.SENT);
        if (senderIds.isEmpty()) return;
        messageRepository.updateStatusForReceiver(me.getId(), MessageStatus.SENT, MessageStatus.DELIVERED);
        for (Long senderId : senderIds) {
            userRepository.findById(senderId).ifPresent(sender ->
                    afterCommit(() -> messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/receipts",
                            new ReceiptEvent("DELIVERED", me.getId()))));
        }
    }

    /** Called when I open a conversation: all messages FROM otherUser TO me become READ. */
    public void markAsRead(String myUsername, Long otherUserId) {
        User me = findByUsername(myUsername);
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        int updated = messageRepository.updateStatusBetween(other.getId(), me.getId(),
                List.of(MessageStatus.SENT, MessageStatus.DELIVERED), MessageStatus.READ);
        if (updated > 0) {
            afterCommit(() -> messagingTemplate.convertAndSendToUser(other.getUsername(), "/queue/receipts",
                    new ReceiptEvent("READ", me.getId())));
        }
    }

    // ------------------------------------------------------------------ reading history

    /**
     * Conversation between ME (taken from the JWT, never from the URL) and another user.
     * Because "me" always comes from the token, nobody can read other people's private chats.
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(String myUsername, Long otherUserId) {
        User me = findByUsername(myUsername);
        if (!userRepository.existsById(otherUserId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return messageRepository.findConversation(me.getId(), otherUserId).stream()
                .map(MessageDto::from).toList();
    }

    /** "Recent conversations" list: last message + unread count per person. */
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(String myUsername) {
        User me = findByUsername(myUsername);
        return messageRepository.findLatestMessagesOf(me.getId()).stream().map(m -> {
            User other = m.getSender().getId().equals(me.getId()) ? m.getReceiver() : m.getSender();
            long unread = messageRepository.countBySenderIdAndReceiverIdAndStatusNot(
                    other.getId(), me.getId(), MessageStatus.READ);
            return new ConversationDto(UserDto.from(other), MessageDto.from(m), unread);
        }).toList();
    }

    /** Runs the action only after the database transaction has been committed (so clients never see "ghost" data). */
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        } else {
            action.run();
        }
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
