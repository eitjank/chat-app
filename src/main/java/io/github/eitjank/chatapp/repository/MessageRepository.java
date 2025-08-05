package io.github.eitjank.chatapp.repository;

import io.github.eitjank.chatapp.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = "SELECT * FROM messages ORDER BY timestamp DESC", nativeQuery = true)
    List<Message> findAllMessagesDesc();

    @Modifying
    @Transactional
    @Query(value = "UPDATE messages SET user_id = ?2 WHERE user_id = ?1", nativeQuery = true)
    void reassignMessagesToAnonymous(Long oldUserId, Long anonymousUserId);

    @Query(value = "SELECT COUNT(*) FROM messages WHERE user_id = ?1", nativeQuery = true)
    int countMessagesByUserId(Long userId);

    @Query(value = "SELECT MIN(timestamp) FROM messages WHERE user_id = ?1", nativeQuery = true)
    LocalDateTime firstMessageTime(Long userId);

    @Query(value = "SELECT MAX(timestamp) FROM messages WHERE user_id = ?1", nativeQuery = true)
    LocalDateTime lastMessageTime(Long userId);

    @Query(value = "SELECT AVG(LENGTH(content)) FROM messages WHERE user_id = ?1", nativeQuery = true)
    Double averageMessageLength(Long userId);

    @Query(value = "SELECT content FROM messages WHERE user_id = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    String lastMessageContent(Long userId);
}