package io.github.eitjank.chatapp.service;

import io.github.eitjank.chatapp.dto.MessageResponse;
import io.github.eitjank.chatapp.entity.Message;
import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.repository.MessageRepository;
import io.github.eitjank.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<MessageResponse> getAllMessagesSorted() {
        List<Message> messages = messageRepository.findAllMessagesDesc();
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse postMessage(User user, String content) {
        Message message = new Message();
        message.setUserId(user.getId());
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return mapToResponse(saved);
    }

    private MessageResponse mapToResponse(Message message) {
        // Lookup username by userId, fallback to "anonymous" if user missing
        String username = userRepository.findById(message.getUserId())
                .map(User::getUsername)
                .orElse("anonymous");

        return new MessageResponse(
                username,
                message.getContent(),
                message.getTimestamp()
        );
    }
}
