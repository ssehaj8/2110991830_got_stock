package com.cg.gotstock.controller;


import com.cg.gotstock.dto.UserRegisterDTO;
import com.cg.gotstock.service.EmailService;
import com.cg.gotstock.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
     private UserService userService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO registerDTO) throws MessagingException {
        emailService.sendEmail(registerDTO.getEmail(), "Registration successful", "Welcome to got-stock, your account has been created");
        return userService.registerUser(registerDTO);
    }
}
