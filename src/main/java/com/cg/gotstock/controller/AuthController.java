package com.cg.gotstock.controller;

import com.cg.gotstock.dto.*;
import com.cg.gotstock.service.EmailService;
import com.cg.gotstock.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO)throws MessagingException{
        return userService.registerUser(registerDTO);
    }
    @PostMapping("/login-user")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO loginDTO){
        return userService.loginUser(loginDTO);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        return userService.resetPassword(resetPasswordDTO);
    }

   @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO)throws MessagingException{
        return userService.forgotPassword(forgotPasswordDTO);
    }


}

