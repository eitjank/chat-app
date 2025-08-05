package io.github.eitjank.chatapp

import io.github.eitjank.chatapp.dto.UserStatsResponse
import io.github.eitjank.chatapp.entity.User
import io.github.eitjank.chatapp.exception.UserAlreadyExistsException
import io.github.eitjank.chatapp.exception.UserNotFoundException
import io.github.eitjank.chatapp.repository.MessageRepository
import io.github.eitjank.chatapp.repository.UserRepository
import io.github.eitjank.chatapp.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    MessageRepository messageRepository = Mock()
    PasswordEncoder passwordEncoder = Mock()

    @Subject
    UserService userService = new UserService(userRepository, messageRepository, passwordEncoder)

    def "registerUser should successfully create new user when username doesn't exist"() {
        given: "a username that doesn't exist"
        String username = "newuser"
        User savedUser = new User(id: 1L, username: username, role: User.Role.USER)

        when: "registering the user"
        User result = userService.registerUser(username, "password123", "USER")

        then: "repository is checked for existing user"
        1 * userRepository.findByUsername(username) >> Optional.empty()

        and: "new user is saved"
        1 * userRepository.save({ User user ->
            user.username == username && user.role == User.Role.USER
        }) >> savedUser

        and: "saved user is returned"
        result == savedUser
    }

    def "registerUser should throw UserAlreadyExistsException when username exists"() {
        given: "a username that already exists"
        String username = "existinguser"
        User existingUser = new User(id: 1L, username: username, role: User.Role.USER)

        when: "trying to register the user"
        userService.registerUser(username, "password123", "USER")

        then: "repository finds existing user"
        1 * userRepository.findByUsername(username) >> Optional.of(existingUser)

        and: "exception is thrown"
        UserAlreadyExistsException ex = thrown()
        ex.message == "User already exists"

        and: "no save operation is performed"
        0 * userRepository.save(_)
    }

    def "findByUsernameOrThrow should return user when username exists"() {
        given: "an existing username"
        String username = "existinguser"
        User existingUser = new User(id: 1L, username: username, role: User.Role.USER)

        when: "finding user by username"
        User result = userService.findByUsernameOrThrow(username)

        then: "repository finds the user"
        1 * userRepository.findByUsername(username) >> Optional.of(existingUser)

        and: "user is returned"
        result == existingUser
    }

    def "findByUsernameOrThrow should throw UserNotFoundException when username doesn't exist"() {
        given: "a non-existing username"
        String username = "nonexistent"

        when: "trying to find user by username"
        userService.findByUsernameOrThrow(username)

        then: "repository doesn't find the user"
        1 * userRepository.findByUsername(username) >> Optional.empty()

        and: "exception is thrown with correct message"
        UserNotFoundException ex = thrown()
        ex.message == "User not found: " + username
    }

    def "deleteUser should successfully delete user and reassign messages"() {
        given: "an existing user and anonymous user"
        String username = "userToDelete"
        User userToDelete = new User(id: 1L, username: username, role: User.Role.USER)
        User anonymousUser = new User(id: 2L, username: "anonymous", role: User.Role.USER)

        when: "deleting the user"
        userService.deleteUser(username)

        then: "user is found"
        1 * userRepository.findByUsername(username) >> Optional.of(userToDelete)

        and: "anonymous user is found"
        1 * userRepository.findByUsername("anonymous") >> Optional.of(anonymousUser)

        and: "messages are reassigned to anonymous"
        1 * messageRepository.reassignMessagesToAnonymous(userToDelete.id, anonymousUser.id)

        and: "user is deleted"
        1 * userRepository.delete(userToDelete)
    }

    def "deleteUser should throw UserNotFoundException when user doesn't exist"() {
        given: "a non-existing username"
        String username = "nonexistent"

        when: "trying to delete the user"
        userService.deleteUser(username)

        then: "user is not found"
        1 * userRepository.findByUsername(username) >> Optional.empty()

        and: "exception is thrown"
        UserNotFoundException ex = thrown()
        ex.message == "User not found"

        and: "no further operations are performed"
        0 * userRepository.findByUsername("anonymous")
        0 * messageRepository.reassignMessagesToAnonymous(_, _)
        0 * userRepository.delete(_)
    }

    def "deleteUser should throw UserNotFoundException when anonymous user doesn't exist"() {
        given: "an existing user but no anonymous user"
        String username = "userToDelete"
        User userToDelete = new User(id: 1L, username: username, role: User.Role.USER)

        when: "trying to delete the user"
        userService.deleteUser(username)

        then: "user is found"
        1 * userRepository.findByUsername(username) >> Optional.of(userToDelete)

        and: "anonymous user is not found"
        1 * userRepository.findByUsername("anonymous") >> Optional.empty()

        and: "exception is thrown"
        UserNotFoundException ex = thrown()
        ex.message == "Anonymous user not found"

        and: "no message reassignment or deletion occurs"
        0 * messageRepository.reassignMessagesToAnonymous(_, _)
        0 * userRepository.delete(_)
    }

    def "getUserStatistics should return statistics for all users"() {
        given: "multiple users in the database"
        User user1 = new User(id: 1L, username: "user1", role: User.Role.USER)
        User user2 = new User(id: 2L, username: "user2", role: User.Role.ADMIN)
        List<User> users = [user1, user2]

        and: "message statistics for users"
        LocalDateTime firstTime1 = LocalDateTime.of(2024, 1, 1, 10, 0)
        LocalDateTime lastTime1 = LocalDateTime.of(2024, 1, 2, 15, 30)
        LocalDateTime firstTime2 = LocalDateTime.of(2024, 1, 3, 9, 0)
        LocalDateTime lastTime2 = LocalDateTime.of(2024, 1, 4, 18, 45)

        when: "getting user statistics"
        List<UserStatsResponse> result = userService.getUserStatistics()

        then: "all users are retrieved"
        1 * userRepository.findAll() >> users

        and: "statistics are retrieved for user1"
        1 * messageRepository.countMessagesByUserId(1L) >> 5
        1 * messageRepository.firstMessageTime(1L) >> firstTime1
        1 * messageRepository.lastMessageTime(1L) >> lastTime1
        1 * messageRepository.averageMessageLength(1L) >> 25.5
        1 * messageRepository.lastMessageContent(1L) >> "Hello world"

        and: "statistics are retrieved for user2"
        1 * messageRepository.countMessagesByUserId(2L) >> 3
        1 * messageRepository.firstMessageTime(2L) >> firstTime2
        1 * messageRepository.lastMessageTime(2L) >> lastTime2
        1 * messageRepository.averageMessageLength(2L) >> 15.0
        1 * messageRepository.lastMessageContent(2L) >> "Admin message"

        and: "correct statistics are returned"
        result.size() == 2

        with(result[0]) {
            username == "user1"
            messageCount == 5
            firstMessageTime == firstTime1
            lastMessageTime == lastTime1
            averageMessageLength == 25.5
            lastMessageText == "Hello world"
        }

        with(result[1]) {
            username == "user2"
            messageCount == 3
            firstMessageTime == firstTime2
            lastMessageTime == lastTime2
            averageMessageLength == 15.0
            lastMessageText == "Admin message"
        }
    }

    def "getUserStatistics should handle null values correctly"() {
        given: "a user with no messages"
        User user = new User(id: 1L, username: "emptyuser", role: User.Role.USER)
        List<User> users = [user]

        when: "getting user statistics"
        List<UserStatsResponse> result = userService.getUserStatistics()

        then: "user is retrieved"
        1 * userRepository.findAll() >> users

        and: "statistics return null/zero values"
        1 * messageRepository.countMessagesByUserId(1L) >> 0
        1 * messageRepository.firstMessageTime(1L) >> null
        1 * messageRepository.lastMessageTime(1L) >> null
        1 * messageRepository.averageMessageLength(1L) >> null
        1 * messageRepository.lastMessageContent(1L) >> null

        and: "null values are handled correctly"
        result.size() == 1
        with(result[0]) {
            username == "emptyuser"
            messageCount == 0
            firstMessageTime == null
            lastMessageTime == null
            averageMessageLength == 0.0  // null converted to 0.0
            lastMessageText == ""         // null converted to empty string
        }
    }

    def "getUserStatistics should return empty list when no users exist"() {
        when: "getting user statistics with no users"
        List<UserStatsResponse> result = userService.getUserStatistics()

        then: "empty user list is returned from repository"
        1 * userRepository.findAll() >> []

        and: "empty statistics list is returned"
        result.isEmpty()

        and: "no message repository calls are made"
        0 * messageRepository._
    }

    def "getUserStatistics should handle partial null values"() {
        given: "a user with some null statistics"
        User user = new User(id: 1L, username: "partialuser", role: User.Role.USER)
        List<User> users = [user]
        LocalDateTime firstTime = LocalDateTime.of(2024, 1, 1, 10, 0)

        when: "getting user statistics"
        List<UserStatsResponse> result = userService.getUserStatistics()

        then: "user is retrieved"
        1 * userRepository.findAll() >> users

        and: "some statistics are available, others are null"
        1 * messageRepository.countMessagesByUserId(1L) >> 2
        1 * messageRepository.firstMessageTime(1L) >> firstTime
        1 * messageRepository.lastMessageTime(1L) >> null
        1 * messageRepository.averageMessageLength(1L) >> null
        1 * messageRepository.lastMessageContent(1L) >> "Some message"

        and: "partial null values are handled correctly"
        result.size() == 1
        with(result[0]) {
            username == "partialuser"
            messageCount == 2
            firstMessageTime == firstTime
            lastMessageTime == null
            averageMessageLength == 0.0
            lastMessageText == "Some message"
        }
    }
}