package com.cg.gotstock.service;

import com.cg.gotstock.dto.ForgotPasswordDTO;
import com.cg.gotstock.dto.ResetPasswordDTO;
import com.cg.gotstock.dto.UserLoginDTO;
import com.cg.gotstock.dto.UserRegisterDTO;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import com.cg.gotstock.security.JwtUtility;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    private static final PasswordEncoder passEncoder = new BCryptPasswordEncoder();

    // Register User
    public ResponseEntity<?> registerUser(UserRegisterDTO registerDTO) throws MessagingException {
        log.info("Registering user with email: {}", registerDTO.getEmail());

        // Check if user already exists
        if (existsByEmail(registerDTO.getEmail())) {
            log.warn("Registration failed: User already exists with email {}", registerDTO.getEmail());
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        // Create a new user
        User user = new User();
        user.setFirstname(registerDTO.getFirstname());
        user.setLastname(registerDTO.getLastname());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhonenumber(registerDTO.getPhonenumber());
        user.setPassword(passEncoder.encode(registerDTO.getPassword()));

        // Save user to database
        userRepository.save(user);
        registerDTO.setId(user.getId());

        log.info("User with email {} registered successfully", user.getEmail());

        // Send confirmation email
        emailService.sendEmail(user.getEmail(), "Registration Successful", "Hello " + user.getFirstname() +
                ",\n\nYou have been successfully registered with GOT-STOCK!");

        return new ResponseEntity<>(registerDTO, HttpStatus.OK);
    }

    // User Login
    public ResponseEntity<?> loginUser(UserLoginDTO loginDTO) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());

            // Generate JWT token
            String jwtToken = jwtUtility.generateToken(userDetails.getUsername());
            log.info("Login successful for user: {}", loginDTO.getEmail());

            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginDTO.getEmail(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    // Reset Password
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        log.info("Password reset requested for email: {}", resetPasswordDTO.getEmail());

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Check OTP validity
        if (user.getOtp() == null || !user.getOtp().equals(resetPasswordDTO.getOtp())) {
            log.warn("Invalid OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("OTP has expired", HttpStatus.BAD_REQUEST);
        }

        // Update password
        user.setPassword(passEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setOtp(null); // Clear OTP after successful reset
        user.setOtpExpiry(null); // Clear OTP expiry
        userRepository.save(user);

        log.info("Password updated successfully for email: {}", resetPasswordDTO.getEmail());
        return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
    }

    // Forgot Password
    public ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) throws MessagingException {
        log.info("Forgot password requested for email: {}", forgotPasswordDTO.getEmail());

        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", forgotPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Generate OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10)); // 10 min expiry
        userRepository.save(user);

        // Send OTP via email
        String subject = "Your OTP for Password Reset";
        String body = "Hello,\n\nYour OTP for resetting your password is: " + otp +
                "\nThis OTP is valid for 10 minutes.\n\nRegards,\nGOT-STOCK Team";
        emailService.sendEmail(user.getEmail(), subject, body);

        log.info("OTP sent to email: {}", forgotPasswordDTO.getEmail());
        return new ResponseEntity<>("OTP sent to email", HttpStatus.OK);
    }

    // Generate OTP using SecureRandom for better security
    private String generateOtp() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 100000 + secureRandom.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return Optional.empty();
    }

    // Use BCryptPasswordEncoder to compare raw and encoded passwords
    @Override
    public boolean matchPassword(String rawPassword, String encodedPassword) {
        return passEncoder.matches(rawPassword, encodedPassword); // Secure password comparison
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
