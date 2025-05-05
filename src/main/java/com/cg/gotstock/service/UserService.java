package com.cg.gotstock.service;

import com.cg.gotstock.dto.*;
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

    /**
     * Registers a new user and sends a confirmation email.
     */
    public ResponseEntity<?> registerUser(UserRegisterDTO registerDTO) throws MessagingException {
        log.info("Registering user with email: {}", registerDTO.getEmail());

        // Check if a user already exists with the given email
        if (existsByEmail(registerDTO.getEmail())) {
            log.warn("Registration failed: User already exists with email {}", registerDTO.getEmail());
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        // Create and populate a new user entity
        User user = new User();
        user.setFirstname(registerDTO.getFirstname());
        user.setLastname(registerDTO.getLastname());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhonenumber(registerDTO.getPhonenumber());
        user.setPassword(passEncoder.encode(registerDTO.getPassword()));

        // Persist user to the database
        userRepository.save(user);
        registerDTO.setId(user.getId());

        log.info("User with email {} registered successfully", user.getEmail());

        // Send registration success email
        emailService.sendEmail(user.getEmail(), "Registration Successful", "Hello " + user.getFirstname() +
                ",\n\nYou have been successfully registered with GOT-STOCK!");

        return new ResponseEntity<>(registerDTO, HttpStatus.OK);
    }

    /**
     * Authenticates a user and returns a JWT token on success.
     */
    public ResponseEntity<?> loginUser(UserLoginDTO loginDTO) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());

        try {
            // Perform authentication using Spring Security
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(), loginDTO.getPassword()));

            // Load user details and generate a JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
            String jwtToken = jwtUtility.generateToken(userDetails.getUsername());

            log.info("Login successful for user: {}", loginDTO.getEmail());

            return new ResponseEntity<>("user logged in successfully\n" + "token:" + jwtToken, HttpStatus.OK);
        } catch (Exception e) {
            // Catch authentication failure
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Resets a user's password after verifying OTP.
     */
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        log.info("Password reset requested for email: {}", resetPasswordDTO.getEmail());

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Validate OTP
        if (user.getOtp() == null || !user.getOtp().equals(resetPasswordDTO.getOtp())) {
            log.warn("Invalid OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        // Check OTP expiration
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP for email: {}", resetPasswordDTO.getEmail());
            return new ResponseEntity<>("OTP has expired", HttpStatus.BAD_REQUEST);
        }

        // Update password and clear OTP
        user.setPassword(passEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        log.info("Password updated successfully for email: {}", resetPasswordDTO.getEmail());
        return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
    }

    /**
     * Generates and emails an OTP to the user for password reset.
     */
    public ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) throws MessagingException {
        log.info("Forgot password requested for email: {}", forgotPasswordDTO.getEmail());

        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail());

        if (user == null) {
            log.warn("No user found with email: {}", forgotPasswordDTO.getEmail());
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Generate and store OTP with expiry
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send OTP email
        String subject = "Your OTP for Password Reset";
        String body = "Hello,\n\nYour OTP for resetting your password is: " + otp +
                " \n\nThis OTP is valid for 10 minutes.\n\nRegards,\nGOT-STOCK Team";
        emailService.sendEmail(user.getEmail(), subject, body);

        log.info("OTP sent to email: {}", forgotPasswordDTO.getEmail());
        return new ResponseEntity<>("OTP sent to email", HttpStatus.OK);
    }

    /**
     * Changes the user's password after validating JWT token and current password.
     */
    public ResponseEntity<?> changePassword(ChangePasswordDTO changePasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();


        User user = userRepository.findByEmail(email);
        if (user == null) {
            return new ResponseEntity<>("Invalid token or user not found", HttpStatus.UNAUTHORIZED);
        }

        // Validate current password
        if (!matchPassword(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            return new ResponseEntity<>("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        // Save new password
        user.setPassword(passEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

    /**
     * Generates a secure 6-digit OTP using SecureRandom.
     */
    private String generateOtp() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 100000 + secureRandom.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    /**
     * Returns user by username — currently not implemented.
     */
    @Override
    public Optional<User> getUserByUsername(String username) {
        return Optional.empty(); // You may want to implement this if needed
    }

    /**
     * Matches raw password with encoded password using BCrypt.
     */
    @Override
    public boolean matchPassword(String rawPassword, String encodedPassword) {
        return passEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Checks if a user exists by email.
     */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
