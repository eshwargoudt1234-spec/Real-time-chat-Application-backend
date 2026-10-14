package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

/** Entry point. Run this class to start the whole backend. */
@CrossOrigin(origins="http://localhost:5173")
@SpringBootApplication
public class RealTimeChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealTimeChatApplication.class, args);
    }
}
