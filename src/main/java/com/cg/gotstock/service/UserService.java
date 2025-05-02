package com.cg.gotstock.service;

import com.cg.gotstock.dto.PortfolioResponseDTO;
import com.cg.gotstock.dto.UserLoginDTO;
import com.cg.gotstock.dto.UserRegisterDTO;
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

        userRepository.save(user);

        log.info("User {} registered successfully!", user.getEmail());
        emailService.sendEmail(user.getEmail(), "Registration Successful", "Hii"
                + "\n You have been successfully Registered in GOT-STOCK");

        return new ResponseEntity<UserRegisterDTO>(registerDTO, HttpStatus.OK);
    }


    public ResponseEntity<?> loginUser(UserLoginDTO loginDTO)  {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        User user = userRepository.findByUsername(loginDTO.getUsername());



            if (matchPassword(loginDTO.getPassword(), user.getPassword())) {
                String token = jwtUtility.generateToken(user.getEmail());

                userRepository.save(user);
                log.debug("Login successful for user: {}- Token generated", user.getEmail());

                return new ResponseEntity<>(token, HttpStatus.OK);

            } else {
                log.warn("Invalid credentials for user: {}", loginDTO.getEmail());

                return new ResponseEntity<>("invalid credentials", HttpStatus.OK);
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
