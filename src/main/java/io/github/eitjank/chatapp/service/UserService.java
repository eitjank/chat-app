package io.github.eitjank.chatapp.service;

import io.github.eitjank.chatapp.dto.UserStatsResponse;
import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.exception.UserAlreadyExistsException;
import io.github.eitjank.chatapp.exception.UserNotFoundException;
import io.github.eitjank.chatapp.repository.MessageRepository;
import io.github.eitjank.chatapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    UserService(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public User registerUser(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    public User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User anonymousUser = userRepository.findByUsername("anonymous")
                .orElseThrow(() -> new UserNotFoundException("Anonymous user not found"));

        messageRepository.reassignMessagesToAnonymous(user.getId(), anonymousUser.getId());
        userRepository.delete(user);
    }

    public List<UserStatsResponse> getUserStatistics() {
        List<User> users = userRepository.findAll();
        List<UserStatsResponse> stats = new ArrayList<>();
        for (User user : users) {
            int count = messageRepository.countMessagesByUserId(user.getId());
            LocalDateTime first = messageRepository.firstMessageTime(user.getId());
            LocalDateTime last = messageRepository.lastMessageTime(user.getId());
            Double avgLen = messageRepository.averageMessageLength(user.getId());
            String lastMsg = messageRepository.lastMessageContent(user.getId());
            // Handle nulls:
            if (avgLen == null) avgLen = 0.0;
            if (lastMsg == null) lastMsg = "";
            stats.add(new UserStatsResponse(user.getUsername(), count, first, last, avgLen, lastMsg));
        }
        return stats;
    }
}