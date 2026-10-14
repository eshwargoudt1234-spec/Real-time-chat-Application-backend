package com.chatapp.service;

import com.chatapp.dto.AuthResponse;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.UserDto;
import com.chatapp.entity.User;
import com.chatapp.exception.InvalidCredentialsException;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String id = req.usernameOrEmail().trim();
        // Same message for "no such user" and "wrong password" so attackers can't guess usernames
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(id, id)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getUsername()), UserDto.from(user));
    }
}
