package io.github.eitjank.chatapp

import io.github.eitjank.chatapp.dto.MessageResponse
import io.github.eitjank.chatapp.entity.Message
import io.github.eitjank.chatapp.entity.User
import io.github.eitjank.chatapp.repository.MessageRepository
import io.github.eitjank.chatapp.repository.UserRepository
import io.github.eitjank.chatapp.service.ChatService
import spock.lang.Specification

import java.time.LocalDateTime

class ChatServiceSpec extends Specification {

    def messageRepository = Mock(MessageRepository)
    def userRepository = Mock(UserRepository)

    def chatService = new ChatService(messageRepository, userRepository)

    def "should return messages sorted and mapped to response"() {
        given:
        def user = new User(id: 1L, username: "testuser")
        def message = new Message(id: 1L, userId: 1L, content: "Hello!", timestamp: LocalDateTime.now())

        messageRepository.findAllMessagesDesc() >> [message]
        userRepository.findById(1L) >> Optional.of(user)

        when:
        List<MessageResponse> result = chatService.getAllMessagesSorted()

        then:
        result.size() == 1
        result[0].username == "testuser"
        result[0].content == "Hello!"
    }

    def "should return sorted messages"() {
        given:
        def messages = [
                new Message(id: 1, userId: 101, content: "Hello", timestamp: LocalDateTime.now().minusMinutes(2)),
                new Message(id: 2, userId: 102, content: "World", timestamp: LocalDateTime.now())
        ]
        messageRepository.findAllMessagesDesc() >> messages
        userRepository.findById(101) >> Optional.of(new User(id: 101, username: "Alice", role: User.Role.USER))
        userRepository.findById(102) >> Optional.of(new User(id: 102, username: "Bob", role: User.Role.USER))

        when:
        List<MessageResponse> result = chatService.getAllMessagesSorted()

        then:
        result.size() == 2
        result[0].username == "Alice"
        result[1].username == "Bob"
    }

    def "should fallback to anonymous if user not found"() {
        given:
        def message = new Message(id: 2L, userId: 99L, content: "Unknown user", timestamp: LocalDateTime.now())

        messageRepository.findAllMessagesDesc() >> [message]
        userRepository.findById(99L) >> Optional.empty()

        when:
        List<MessageResponse> result = chatService.getAllMessagesSorted()

        then:
        result.size() == 1
        result[0].username == "anonymous"
        result[0].content == "Unknown user"
    }

    def "should fallback to anonymous if user deleted after posting"() {
        given:
        def user = new User(id: 200, username: "TempUser", role: User.Role.USER)
        def msg = new Message(id: 3, userId: 200, content: "Forgotten", timestamp: LocalDateTime.now())
        messageRepository.save(_) >> msg
        userRepository.findById(200) >> Optional.empty()

        when:
        def response = chatService.postMessage(user, "Forgotten")

        then:
        response.username == "anonymous"
    }

    def "should post a message and return response"() {
        given:
        def user = new User(id: 5L, username: "poster")
        def content = "Test message"
        def message = new Message(id: 10L, userId: 5L, content: content, timestamp: LocalDateTime.now())

        messageRepository.save(_) >> message
        userRepository.findById(5L) >> Optional.of(user)

        when:
        def response = chatService.postMessage(user, content)

        then:
        response.username == "poster"
        response.content == "Test message"
    }
}
