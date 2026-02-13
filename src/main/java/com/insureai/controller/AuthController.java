package com.insureai.controller;

import com.insureai.model.User;
import com.insureai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Optional<User> loggedUser =
                userService.login(user.getEmail(), user.getPassword());

        if (loggedUser.isPresent()) {
            return "Login successful";
        } else {
            return "Invalid credentials";
        }
    }
}

