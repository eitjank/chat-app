package io.github.eitjank.chatapp.controller;

import io.github.eitjank.chatapp.dto.UserRequest;
import io.github.eitjank.chatapp.dto.UserStatsResponse;
import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin operations like user registration, deletion, and statistics")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @Operation(summary = "Register new user", description = "Registers a new user with the provided username")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRequest request) {
        User newUser = userService.registerUser(request.getUsername());
        return ResponseEntity.ok(newUser);
    }

    @DeleteMapping("/users/{username}")
    @Operation(summary = "Delete user", description = "Deletes a user by username and reassigns their messages to 'anonymous'")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Retrieves message statistics for all users")
    public ResponseEntity<List<UserStatsResponse>> getAllUserStats() {
        List<UserStatsResponse> stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
}