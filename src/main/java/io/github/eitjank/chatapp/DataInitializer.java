package io.github.eitjank.chatapp;

import io.github.eitjank.chatapp.entity.User;
import io.github.eitjank.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("anonymous").isEmpty()) {
            User anonymous = new User();
            anonymous.setUsername("anonymous");
            anonymous.setRole(User.Role.USER);
            anonymous.setPassword(passwordEncoder.encode("dummy"));  // required field
            userRepository.save(anonymous);
            System.out.println("Anonymous user created.");
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Initial admin user created.");
        }
    }
}