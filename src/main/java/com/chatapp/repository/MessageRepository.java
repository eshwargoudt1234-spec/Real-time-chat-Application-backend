package com.chatapp.repository;

import com.chatapp.entity.Message;
import com.chatapp.entity.MessageStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Full conversation between two users, oldest first */
    @Query("select m from Message m where " +
           "(m.sender.id = :a and m.receiver.id = :b) or (m.sender.id = :b and m.receiver.id = :a) " +
           "order by m.timestamp asc, m.id asc")
    List<Message> findConversation(@Param("a") Long userA, @Param("b") Long userB);

    /** The newest message of each conversation the user takes part in (MySQL native SQL) */
    @Query(value = "SELECT m.* FROM messages m WHERE m.id IN (" +
            " SELECT MAX(id) FROM messages WHERE sender_id = :uid OR receiver_id = :uid " +
            " GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) " +
            "ORDER BY m.id DESC", nativeQuery = true)
    List<Message> findLatestMessagesOf(@Param("uid") Long userId);

    long countBySenderIdAndReceiverIdAndStatusNot(Long senderId, Long receiverId, MessageStatus status);

    /** Which users sent me messages with this status? */
    @Query("select distinct m.sender.id from Message m where m.receiver.id = :rid and m.status = :status")
    List<Long> findSenderIdsByReceiverAndStatus(@Param("rid") Long receiverId, @Param("status") MessageStatus status);

    /** SENT -> DELIVERED for everything addressed to a user */
    @Modifying(clearAutomatically = true)
    @Query("update Message m set m.status = :newStatus where m.receiver.id = :rid and m.status = :oldStatus")
    int updateStatusForReceiver(@Param("rid") Long receiverId,
                                @Param("oldStatus") MessageStatus oldStatus,
                                @Param("newStatus") MessageStatus newStatus);

    /** Mark messages from one sender to one receiver as READ */
    @Modifying(clearAutomatically = true)
    @Query("update Message m set m.status = :newStatus where m.sender.id = :sid and m.receiver.id = :rid " +
           "and m.status in :oldStatuses")
    int updateStatusBetween(@Param("sid") Long senderId, @Param("rid") Long receiverId,
                            @Param("oldStatuses") Collection<MessageStatus> oldStatuses,
                            @Param("newStatus") MessageStatus newStatus);
}
