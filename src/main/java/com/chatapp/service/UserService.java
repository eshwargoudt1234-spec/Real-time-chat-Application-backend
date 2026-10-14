package com.chatapp.service;

import com.chatapp.dto.RegisterRequest;
import com.chatapp.dto.UserDto;
import com.chatapp.entity.User;
import com.chatapp.entity.UserStatus;
import com.chatapp.exception.BadRequestException;
import com.chatapp.exception.DuplicateResourceException;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates a new account with a BCrypt-hashed password. */
    public UserDto register(RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        String username = req.username().trim();
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = new User();
        user.setName(req.name().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));   // hashed, never plain text
        try {
            return UserDto.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            // two people registered the same name at the exact same moment
            throw new DuplicateResourceException("Username or email is already in use");
        }
    }

    @Transactional(readOnly = true)
    public UserDto getByUsername(String username) {
        return UserDto.from(findEntityByUsername(username));
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userRepository.findById(id).map(UserDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** All users except me; optional search by name/username. */
    @Transactional(readOnly = true)
    public List<UserDto> listUsers(String myUsername, String search) {
        List<User> users = (search == null || search.isBlank())
                ? userRepository.findByUsernameNotOrderByNameAsc(myUsername)
                : userRepository.search(myUsername, search.trim());
        return users.stream().map(UserDto::from).toList();
    }

    public UserDto updateStatus(String username, UserStatus status) {
        User user = findEntityByUsername(username);
        user.setStatus(status);
        return UserDto.from(userRepository.save(user));
    }

    private User findEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
