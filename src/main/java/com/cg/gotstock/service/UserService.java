package com.cg.gotstock.service;

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
        user.setPassword(passEncoder.encode(registerDTO.getPassword()));

        userRepository.save(user);
        registerDTO.setId(user.getId());
        log.info("User {} registered successfully!", user.getEmail());
        emailService.sendEmail(user.getEmail(), "Registration Successful", "Hii"
                + "\n You have been successfully Registered in GOT-STOCK");

        return new ResponseEntity<UserRegisterDTO>(registerDTO, HttpStatus.OK);
    }


    public ResponseEntity<?> loginUser(UserLoginDTO loginDTO)  {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            UserDetails userDetails= userDetailsService.loadUserByUsername(loginDTO.getEmail());
            String jwtToken= jwtUtility.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

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
