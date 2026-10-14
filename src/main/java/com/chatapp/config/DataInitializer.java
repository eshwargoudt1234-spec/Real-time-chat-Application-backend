package com.chatapp.config;

import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.chatapp.entity.UserStatus;

/** Runs once at startup: resets everyone to OFFLINE and creates 3 demo users if the table is empty. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.updateAllStatus(UserStatus.OFFLINE);
        if (userRepository.count() == 0) {
            createUser("Alice Johnson", "alice", "alice@example.com");
            createUser("Bob Smith", "bob", "bob@example.com");
            createUser("Charlie Brown", "charlie", "charlie@example.com");
            log.info("Demo users created: alice, bob, charlie (password: Password@123)");
        }
    }

    private void createUser(String name, String username, String email) {
        User u = new User();
        u.setName(name);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Password@123"));
        userRepository.save(u);
    }
}
