package com.chatapp.controller;

import com.chatapp.dto.UserDto;
import com.chatapp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Private endpoints (JWT required). */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users            -> every other user
     *  GET /api/users?search=ali -> filtered by name/username */
    @GetMapping
    public List<UserDto> list(@RequestParam(required = false) String search, Authentication auth) {
        return userService.listUsers(auth.getName(), search);
    }

    /** GET /api/users/me -> my own profile */
    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        return userService.getByUsername(auth.getName());
    }

    /** GET /api/users/{id} */
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return userService.getById(id);
    }
}
