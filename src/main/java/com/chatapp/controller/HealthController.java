package com.chatapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Open http://localhost:8080 in a browser to check the backend is alive. */
@RestController
public class HealthController {

    @GetMapping({"/", "/api/health"})
    public Map<String, String> health() {
        return Map.of("status", "UP", "app", "Pulse Chat backend");
    }
}
