package io.github.eitjank.chatapp;

import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("anonymous").isEmpty()) {
            User anonymous = new User();
            anonymous.setUsername("anonymous");
            anonymous.setRole(User.Role.USER);  // or a special role if you want
            userRepository.save(anonymous);
        }
    }
}