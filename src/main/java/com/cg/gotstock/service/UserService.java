package com.cg.gotstock.service;

import com.cg.gotstock.dto.*;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import com.cg.gotstock.utility.JwtUtility;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class UserService implements UserInterface {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtUtility jwtUtility;

    public ResponseEntity<?> registerUser(UserRegisterDTO registerDTO)throws MessagingException  {
        log.info("Registering user: {}", registerDTO.getEmail());


        if (existsByEmail(registerDTO.getEmail())) {
            log.warn("Registration failed: User already exists with email {}", registerDTO.getEmail());
            return new ResponseEntity<>("registration failed", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setFirstname(registerDTO.getFirstname());
        user.setLastname(registerDTO.getLastname());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhonenumber(registerDTO.getPhonenumber());
        user.setPassword(registerDTO.getPassword());

         User savedUser=userRepository.save(user);

        log.info("User {} registered successfully!", user.getEmail());
        emailService.sendEmail(user.getEmail(), "Registration Successful", "Hii"
                + "\n You have been successfully Registered in GOT-STOCK");

        registerDTO.setId(savedUser.getId());

        return new ResponseEntity<UserRegisterDTO>(registerDTO, HttpStatus.OK);
    }


    public ResponseEntity<?> loginUser(UserLoginDTO loginDTO)  {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        User user = userRepository.findByUsername(loginDTO.getUsername());



            if (matchPassword(loginDTO.getPassword(), user.getPassword())) {
                String token = jwtUtility.generateToken(user.getEmail());

                userRepository.save(user);
                log.debug("Login successful for user: {}- Token generated", user.getEmail());

                return new ResponseEntity<>("User Logged in successfully   Token: "+token, HttpStatus.OK);

            } else {
                log.warn("Invalid credentials for user: {}", loginDTO.getEmail());

                return new ResponseEntity<>("invalid credentials", HttpStatus.OK);
            }

        }
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        log.info("Password reset requested for email: {}", resetPasswordDTO.getEmail());

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }


        if (user.getOtp() == null || !user.getOtp().equals(resetPasswordDTO.getOtp())) {
            log.warn("Invalid OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("OTP has expired", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(resetPasswordDTO.getNewPassword()); // Or encode if needed
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        log.info("Password updated successfully for email: {}", resetPasswordDTO.getEmail());
        return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) throws MessagingException{
        log.info("Forgot password requested for email: {}", forgotPasswordDTO.getEmail());

        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", forgotPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }


        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10)); // 10 min expiry
        userRepository.save(user);

        String subject = "Your OTP for Password Reset";
        String body = "Hello,\n\nYour OTP for resetting your password is: " + otp +
                "\nThis OTP is valid for 10 minutes.\n\nRegards,\nGOT-STOCK Team";
        emailService.sendEmail(user.getEmail(), subject, body);

        log.info("OTP sent to email: {}", forgotPasswordDTO.getEmail());
        return new ResponseEntity<>("OTP sent to email", HttpStatus.OK);
    }
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }



    @Override
    public Optional<User> getUserByUsername(String username) {
        return Optional.empty();
    }


    @Override
    public boolean matchPassword(String rawPassword, String encodedPassword) {
        // Use equals() if passwords are plain text (not recommended)
        return rawPassword.equals(encodedPassword);
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }
}
