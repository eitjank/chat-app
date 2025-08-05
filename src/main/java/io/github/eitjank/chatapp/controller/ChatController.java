package io.github.eitjank.chatapp.controller;


import io.github.eitjank.chatapp.dto.MessageRequest;
import io.github.eitjank.chatapp.dto.MessageResponse;
import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.service.ChatService;
import io.github.eitjank.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Messages", description = "Endpoints for chat messages")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Returns all chat messages in reverse chronological order")
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<MessageResponse> messages = chatService.getAllMessagesSorted();
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    @Operation(summary = "Post a new message", description = "Creates a new chat message")
    public ResponseEntity<MessageResponse> postMessage(@RequestBody MessageRequest request) {
        // Extract username from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch user and post message
        User user = userService.findByUsernameOrThrow(username);
        MessageResponse response = chatService.postMessage(user, request.getContent());
        return ResponseEntity.ok(response);
    }
}
